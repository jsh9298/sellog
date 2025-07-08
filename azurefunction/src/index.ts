import { InvocationContext } from "@azure/functions";
import { BlobServiceClient } from "@azure/storage-blob";
import sharp from "sharp";
import * as path from "path";
import * as os from "os";
import * as fs from "fs/promises";
import { spawn } from "child_process";
import ffmpegPath from "ffmpeg-static";

const eventGridTrigger = async function (
  context: InvocationContext,
  event: any
): Promise<void> {
  const blobUrl: string | undefined = event.data?.url;
  if (!blobUrl) {
    context.error("Missing blob URL in event data.");
    return;
  }

  // 🎯 inputcontents 컨테이너만 처리
  if (!blobUrl.includes("/inputcontents/")) {
    context.log(`Skipping blob from unrelated container: ${blobUrl}`);
    return;
  }

  const parsedUrl = new URL(blobUrl);
  const pathParts = parsedUrl.pathname.split("/").filter(Boolean); // ['inputcontents', '{userId}', '{filename}']
  const containerName = pathParts[0];
  const userId = pathParts[1];
  const blobRelativePath = pathParts.slice(2).join("/"); // 파일명 또는 경로 포함된 이름

  if (!userId || !blobRelativePath) {
    context.error(
      "Invalid blob path. Expected format: inputcontents/{userId}/{filename}"
    );
    return;
  }

  const connectionString = process.env.AzureWebJobsStorage!;
  const blobServiceClient =
    BlobServiceClient.fromConnectionString(connectionString);
  const inputContainerClient =
    blobServiceClient.getContainerClient(containerName);
  const inputBlobClient = inputContainerClient.getBlobClient(
    `${userId}/${blobRelativePath}`
  );

  context.log(`Processing blob: ${blobRelativePath} for user: ${userId}`);

  const downloadResponse = await inputBlobClient.download();
  if (!downloadResponse.readableStreamBody) {
    context.error(`Failed to download blob stream: ${blobRelativePath}`);
    return;
  }

  const buffer = await streamToBuffer(downloadResponse.readableStreamBody);

  const ext = path.extname(blobRelativePath).toLowerCase().slice(1);
  const imageExt = ["jpg", "jpeg", "png", "gif", "bmp", "webp"];
  const videoExt = ["mp4", "mov", "avi", "mkv", "wmv"];
  let bufferForThumb: Buffer = buffer;

  const outputContainerClient =
    blobServiceClient.getContainerClient("outcontents");

  const baseName = path.basename(blobRelativePath);

  // 🎯 썸네일 경로
  const thumbnailPath = `${userId}/thumbnails/${baseName}`;
  const originPath = `${userId}/origin/${baseName}`;
  const miscPath = `${userId}/files/${baseName}`;

  // 썸네일 대상 파일인 경우
  if (imageExt.includes(ext) || videoExt.includes(ext)) {
    if (videoExt.includes(ext)) {
      context.log(`Video file detected: ${baseName}. Extracting frame.`);
      const tmpDir = await fs.mkdtemp(path.join(os.tmpdir(), "thumb-"));
      const inputPath = path.join(tmpDir, "input.mp4");
      const outputPath = path.join(tmpDir, "frame.jpg");

      await fs.writeFile(inputPath, buffer);
      try {
        await new Promise<void>((resolve, reject) => {
          const proc = spawn(ffmpegPath!, [
            "-ss",
            "0.5",
            "-i",
            inputPath,
            "-frames:v",
            "1",
            "-q:v",
            "2",
            outputPath,
          ]);

          proc.stderr.on("data", (data) => {
            context.log(`FFmpeg: ${data.toString()}`);
          });

          proc.on("close", async (code) => {
            if (code === 0) {
              bufferForThumb = await fs.readFile(outputPath);
              resolve();
            } else {
              reject(new Error(`FFmpeg exited with code ${code}`));
            }
          });

          proc.on("error", (err) => {
            reject(new Error(`Failed to start FFmpeg: ${err.message}`));
          });
        });
      } finally {
        await fs.rm(tmpDir, { recursive: true, force: true });
        context.log(`Cleaned temp directory: ${tmpDir}`);
      }
    } else {
      context.log(`Image file detected: ${baseName}`);
    }

    // 썸네일 생성
    const thumbnailBuffer = await sharp(bufferForThumb)
      .resize(200, 200, { fit: "inside", withoutEnlargement: true })
      .jpeg({ quality: 80 })
      .toBuffer();

    // 썸네일 업로드
    await outputContainerClient
      .getBlockBlobClient(thumbnailPath)
      .upload(thumbnailBuffer, thumbnailBuffer.length, {
        blobHTTPHeaders: { blobContentType: "image/jpeg" },
      });

    context.log(`✅ Thumbnail uploaded to: outcontents/${thumbnailPath}`);

    // 원본 업로드
    await outputContainerClient
      .getBlockBlobClient(originPath)
      .upload(buffer, buffer.length, {
        blobHTTPHeaders: {
          blobContentType: downloadResponse.contentType ?? undefined,
        },
      });

    context.log(`📦 Original file uploaded to: outcontents/${originPath}`);
  } else {
    // 미지원 파일은 files 디렉토리에 업로드
    context.log(`Unsupported type: ${baseName}. Uploading to files/...`);

    await outputContainerClient
      .getBlockBlobClient(miscPath)
      .upload(buffer, buffer.length, {
        blobHTTPHeaders: {
          blobContentType: downloadResponse.contentType ?? undefined,
        },
      });

    context.log(`📁 File uploaded to: outcontents/${miscPath}`);
  }
};

export default eventGridTrigger;

// Stream → Buffer 변환
async function streamToBuffer(stream: NodeJS.ReadableStream): Promise<Buffer> {
  const chunks: Buffer[] = [];
  for await (const chunk of stream) {
    chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk));
  }
  return Buffer.concat(chunks);
}

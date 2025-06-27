import { app, InvocationContext, output } from "@azure/functions";
import sharp from "sharp";
import ffmpeg from "fluent-ffmpeg";
import ffmpegStatic from "ffmpeg-static";
import * as os from "os";
import * as path from "path";
import * as fs from "fs/promises";
import { BlobServiceClient } from "@azure/storage-blob";

ffmpeg.setFfmpegPath(ffmpegStatic!);
// Output 바인딩 정의
const thumbnails = output.storageBlob({
  path: "OutContents/{userId}/thumbnails/{filename}.jpg",
  connection: "AzureWebJobsStorage",
});
const origin = output.storageBlob({
  path: "OutContents/{userId}/origin/{filename}",
  connection: "AzureWebJobsStorage",
});
const postContents = output.storageBlob({
  path: "OutContents/{userId}/{filename}",
  connection: "AzureWebJobsStorage",
});

async function extractFrame(input: Buffer, tmpDir: string): Promise<Buffer> {
  const inputPath = path.join(tmpDir, "input-video");
  const outputPath = path.join(tmpDir, "frame.jpg");

  await fs.writeFile(inputPath, input);
  return new Promise<Buffer>((resolve, reject) => {
    ffmpeg(inputPath)
      .outputOptions(["-ss 0.5", "-frames:v 1"])
      .output(outputPath)
      .on("end", async () => {
        try {
          const buf = await fs.readFile(outputPath);
          resolve(buf);
        } catch (e) {
          reject(e);
        }
      })
      .on("error", reject)
      .run();
  });
}

async function blobHandler(
  inputBlob: Buffer,
  context: InvocationContext
): Promise<void> {
  const userId = context.triggerMetadata.userId as string;
  const filename = context.triggerMetadata.filename as string;
  const ext = filename.split(".").pop()?.toLowerCase();
  context.log(
    ` Start processing: ${userId}/${filename} (${inputBlob.length} bytes)`
  );
  const imageExt = ["jpg", "jpeg", "png", "gif", "bmp", "webp"];
  const videoExt = ["mp4", "mov", "avi", "mkv", "wmv"];
  const isImage = ext && imageExt.includes(ext);
  const isVideo = ext && videoExt.includes(ext);

  try {
    if (!isImage && !isVideo) {
      context.log(`Unsupported thumbnail generate file type: .${ext}`);
      context.extraOutputs.set(postContents, inputBlob);
      context.log(`Upload successfully`);
    } else {
      let bufferForThumb: Buffer;
      if (isImage) {
        bufferForThumb = inputBlob;
        context.log("Detected as image");
      } else {
        context.log("Detected as video; extracting first frame");
        const tmpdir = await fs.mkdtemp(path.join(os.tmpdir(), "thumb-"));
        bufferForThumb = await extractFrame(inputBlob, tmpdir);
        context.log("Frame extracted");
      }
      const thumbnail = await sharp(bufferForThumb)
        .resize(200, 200, { fit: "inside" })
        .jpeg()
        .toBuffer();
      context.extraOutputs.set(thumbnails, thumbnail);
      context.extraOutputs.set(origin, bufferForThumb);
      context.log(`Thumbnails generated successfully`);
    }

    await deleteInputBlob(userId, filename, context);
  } catch (err) {
    context.error("Error during thumbnail processing:", err);
    throw err;
  }
}

async function deleteInputBlob(
  userId: string,
  filename: string,
  context: InvocationContext
) {
  const connectionString = process.env["AzureWebJobsStorage"];
  if (!connectionString) {
    context.warn("AzureWebJobsStorage not set");
    return;
  }
  const blobServiceClient =
    BlobServiceClient.fromConnectionString(connectionString);
  const containerClient = blobServiceClient.getContainerClient("InputContents");
  const blobClient = containerClient.getBlobClient(`${userId}/${filename}`);
  try {
    await blobClient.deleteIfExists();
    context.log(`Deleted original blob: inputcontents/${userId}/${filename}`);
  } catch (err) {
    context.error("Failed to delete original blob:", err);
  }
}

// 해당 storage container(images)에 blob이 업로드되면 트리거
app.storageBlob("thumbnailTrigger", {
  path: "InputContents/{userId}/{filename}",
  connection: "AzureWebJobsStorage",
  handler: blobHandler,
  extraOutputs: [thumbnails, origin, postContents],
});

import ffmpegStatic from "ffmpeg-static";
import sharp from "sharp";
import { spawn } from "child_process";
import * as os from "os";
import * as path from "path";
import * as fs from "fs/promises";
import { Readable } from "stream";

async function extractFrame(inputBuffer: Buffer, tmpDir: string): Promise<Buffer> {
    const inputPath = path.join(tmpDir, "input.mp4");
    const outputPath = path.join(tmpDir, "frame.jpg");
    const timestamp = process.env.FFMPEG_TIMESTAMP_SEC || "0.5";

    await fs.writeFile(inputPath, inputBuffer);

    return new Promise<Buffer>((resolve, reject) => {
        const ffmpegPath = ffmpegStatic!;
        const args = [
            "-ss", timestamp,
            "-i", inputPath,
            "-frames:v", "1",
            "-q:v", "2",
            outputPath,
        ];

        const proc = spawn(ffmpegPath, args);

        proc.stderr.on("data", (data) => {
            console.error(`[ffmpeg stderr]: ${data}`);
        });

        proc.on("close", async (code) => {
            if (code !== 0) {
                return reject(new Error(`ffmpeg exited with code ${code}`));
            }
            try {
                const buf = await fs.readFile(outputPath);
                resolve(buf);
            } catch (e: any) {
                reject(new Error(`Failed to read output frame: ${e.message}`));
            }
        });

        proc.on("error", (err) => {
            reject(new Error(`Failed to start ffmpeg: ${err.message}`));
        });
    });
}

module.exports = async function (
    context: any,
    inputBlob: Buffer
): Promise<void> {
    const userId = context.bindingData.userId;
    const filename = context.bindingData.filename;

    if (!userId || !filename) {
        context.log.error("Missing userId or filename in bindingData");
        throw new Error("Missing userId or filename in bindingData");
    }

    context.log(`[${userId}/${filename}] Processing started.`);

    const ext = filename.split(".").pop()?.toLowerCase() || "";

    const imageExt = ["jpg", "jpeg", "png", "gif", "bmp", "webp"];
    const videoExt = ["mp4", "mov", "avi", "mkv", "wmv"];
    const isImage = imageExt.includes(ext);
    const isVideo = videoExt.includes(ext);

    try {
        if (!isImage && !isVideo) {
            // 이미지/비디오가 아니면 원본만 저장
            context.bindings.postContentsBlob = inputBlob;
            context.log(`[${userId}/${filename}] Unsupported file type .${ext}, saved original only.`);
        } else {
            let bufferForThumb: Buffer;

            if (isImage) {
                bufferForThumb = inputBlob;
                context.log(`[${userId}/${filename}] Detected image.`);
            } else {
                context.log(`[${userId}/${filename}] Detected video. Extracting frame.`);

                const tmpdir = await fs.mkdtemp(path.join(os.tmpdir(), "thumb-"));
                try {
                    bufferForThumb = await extractFrame(inputBlob, tmpdir);
                    context.log(`[${userId}/${filename}] Frame extracted.`);
                } finally {
                    await fs.rm(tmpdir, { recursive: true, force: true });
                    context.log(`[${userId}/${filename}] Temp dir cleaned up: ${tmpdir}`);
                }
            }

            const thumbnail = await sharp(bufferForThumb)
                .resize(200, 200, { fit: "inside", withoutEnlargement: true })
                .jpeg({ quality: 80 })
                .toBuffer();

            context.bindings.thumbnailBlob = thumbnail;
            context.bindings.originBlob = bufferForThumb;
            context.log(`[${userId}/${filename}] Thumbnail and origin saved.`);
        }

        // 입력 blob 삭제 요청 (빈 값 할당)
        context.bindings.deleteInputBlob = null;
        context.log(`[${userId}/${filename}] Requested deletion of input blob.`);

    } catch (err) {
        context.log.error(`[${userId}/${filename}] Error occurred:`, err);
        // 입력 blob 삭제 요청 (빈 값 할당)
        context.bindings.deleteInputBlob = null;
        throw err;
    }
};

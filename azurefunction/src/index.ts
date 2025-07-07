import { InvocationContext } from "@azure/functions"; // [1] Azure Functions v4: InvocationContext 사용
import { BlobClient } from "@azure/storage-blob"; // [2] Azure Storage Blob SDK
import sharp from "sharp"; // [3] sharp 라이브러리: default import
import * as path from "path";
import * as os from "os";
import * as fs from "fs/promises";
import { spawn } from "child_process";
import ffmpegPath from "ffmpeg-static";

/**
 * Event Grid 트리거 함수: Blob Storage 이벤트에 반응하여 미디어 파일을 처리합니다.
 * - 이미지/비디오 썸네일 생성 및 업로드
 * - 비디오의 경우 첫 프레임 추출
 * @param context Azure Functions 실행 컨텍스트
 * @param event Event Grid 이벤트 데이터
 */
const eventGridTrigger = async function (
    context: InvocationContext,
    event: any
): Promise<void> {
    const blobUrl: string | undefined = event.data?.url; // Blob URL 추출, undefined 가능성 명시

    if (!blobUrl) {
        context.error("Missing blob URL in event data.");
        return;
    }

    // BlobClient 생성: Blob URL을 사용하여 클라이언트를 초기화합니다.
    // 이 URL에는 Blob Storage에 접근할 수 있는 SAS 토큰 또는 다른 인증 정보가 포함되어야 합니다.
    // 또는 BlobServiceClient을 통해 인증된 BlobClient를 생성해야 합니다.
    const blobClient = new BlobClient(blobUrl);

    const parsedBlobUrl = new URL(blobUrl);
    const blobName = path.basename(parsedBlobUrl.pathname);
    const containerName = blobClient.containerName;

    context.log(`Processing blob: ${blobName} from container: ${containerName}`);

    // Blob 다운로드: readableStreamBody가 존재하는지 확인
    const downloadBlockBlobResponse = await blobClient.download();
    if (!downloadBlockBlobResponse.readableStreamBody) {
        context.error(
            `Failed to download blob stream for ${blobName}. Readable stream body is null.`
        );
        return;
    }
    const buffer = await streamToBuffer(
        downloadBlockBlobResponse.readableStreamBody
    );

    // 파일 확장자 추출 및 파일 타입 분류
    const ext = path.extname(blobName).toLowerCase().slice(1);
    const imageExt = ["jpg", "jpeg", "png", "gif", "bmp", "webp"];
    const videoExt = ["mp4", "mov", "avi", "mkv", "wmv"];

    let bufferForThumb: Buffer = buffer; // 썸네일 생성에 사용될 버퍼, 초기값은 원본 버퍼

    // 비디오 파일 처리 로직
    if (videoExt.includes(ext)) {
        context.log(`Video file detected: ${blobName}. Extracting first frame.`);
        const tmpDir = await fs.mkdtemp(path.join(os.tmpdir(), "thumb-")); // 임시 디렉토리 생성
        const inputPath = path.join(tmpDir, "input.mp4");
        const outputPath = path.join(tmpDir, "frame.jpg");

        await fs.writeFile(inputPath, buffer); // 다운로드한 비디오 버퍼를 임시 파일로 저장

        try {
            await new Promise<void>((resolve, reject) => {
                // FFmpeg 실행: ffmpeg-static이 제공하는 바이너리를 직접 spawn
                // '-ss 0.5'는 0.5초 지점, '-frames:v 1'은 단일 비디오 프레임, '-q:v 2'는 품질 설정
                const ffmpegProcess = spawn(ffmpegPath!, [
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

                // FFmpeg 표준 에러(stderr) 출력을 로그에 기록 (디버깅 용이)
                ffmpegProcess.stderr.on("data", (data) => {
                    context.log(`FFmpeg stderr: ${data.toString()}`);
                });

                // FFmpeg 프로세스 종료 이벤트 핸들링
                ffmpegProcess.on("close", async (code) => {
                    if (code === 0) {
                        // 성공적으로 종료
                        try {
                            bufferForThumb = await fs.readFile(outputPath); // 추출된 프레임 이미지 읽기
                            resolve();
                        } catch (readError: unknown) {
                            // 파일 읽기 오류 처리
                            const message =
                                readError instanceof Error
                                    ? readError.message
                                    : String(readError);
                            reject(
                                new Error(`Failed to read extracted frame file: ${message}`)
                            );
                        }
                    } else {
                        // 비정상 종료
                        reject(
                            new Error(
                                `FFmpeg process exited with code ${code}. Check FFmpeg stderr for details.`
                            )
                        );
                    }
                });

                // FFmpeg 프로세스 시작 오류 처리 (예: ffmpeg 바이너리 경로 문제)
                ffmpegProcess.on("error", (spawnError: Error) => {
                    reject(
                        new Error(
                            `Failed to start FFmpeg process: ${spawnError.message}. Ensure ffmpeg-static is correctly installed.`
                        )
                    );
                });
            });
            context.log("Video frame extracted successfully.");
        } finally {
            // 임시 디렉토리 및 파일 정리 (오류 발생 여부와 관계없이 실행)
            await fs.rm(tmpDir, { recursive: true, force: true });
            context.log(`Temporary directory cleaned: ${tmpDir}`);
        }
    } else if (imageExt.includes(ext)) {
        // 이미지 파일은 bufferForThumb이 이미 원본 버퍼를 가지고 있으므로 추가 처리 불필요
        context.log(`Image file detected: ${blobName}.`);
    } else {
        // 지원하지 않는 파일 형식은 썸네일 생성 건너뛰고 함수 종료
        context.log(
            `Unsupported file type for thumbnail generation: ${blobName}. Skipping thumbnail creation.`
        );
        return;
    }

    // 썸네일 생성: sharp 라이브러리 사용
    const thumbnailBuffer = await sharp(bufferForThumb)
        .resize(200, 200, { fit: "inside", withoutEnlargement: true }) // 200x200 크기, 원본보다 커지지 않게
        .jpeg({ quality: 80 }) // JPEG 형식, 품질 80
        .toBuffer();

    // 썸네일 저장 경로 구성: /inputcontents/ -> /outcontents/thumbnails/ 로 변경
    // 예: blobUrl이 "https://account.blob.core.windows.net/inputcontents/user1/image.jpg"
    // -> targetBlobUrl은 "https://account.blob.core.windows.net/outcontents/user1/thumbnails/image.jpg"
    const targetBlobUrl = blobUrl
        .replace(/\/inputcontents\//, "/outcontents/")
        .replace(blobName, `thumbnails/${blobName}`);
    const targetBlob = new BlobClient(targetBlobUrl);
    const blockBlobClient = targetBlob.getBlockBlobClient();

    // 썸네일 업로드: Buffer를 Blob으로 업로드
    // [4] 공식 문서에 따라 upload(data, size) 메서드 사용
    await blockBlobClient.upload(thumbnailBuffer, thumbnailBuffer.length);

    context.log(`Thumbnail uploaded to: ${targetBlob.url}`);
};

// Azure Functions 런타임에 함수를 내보냅니다.
export default eventGridTrigger;

/**
 * Node.js ReadableStream을 Buffer로 변환하는 헬퍼 함수.
 * @param readableStream 변환할 ReadableStream
 * @returns 변환된 Buffer
 */
async function streamToBuffer(
    readableStream: NodeJS.ReadableStream
): Promise<Buffer> {
    const chunks: Buffer[] = [];
    for await (const chunk of readableStream) {
        chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk));
    }
    return Buffer.concat(chunks);
}

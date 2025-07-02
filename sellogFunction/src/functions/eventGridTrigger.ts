import {
    app,
    InvocationContext,
    output,
    EventGridEvent,
} from "@azure/functions";
import { BlobServiceClient } from "@azure/storage-blob"; // Blob Storage SDK
// import sharp from "sharp";
import sharp = require("sharp");
import ffmpeg from "fluent-ffmpeg";
import ffmpegStatic from "ffmpeg-static";
import * as os from "os";
import * as path from "path";
import * as fs from "fs/promises";
import { Readable } from "stream"; // ReadableStream을 다루기 위해 필요

// BlobEventData 인터페이스를 직접 정의하여 타입 안정성을 유지합니다.
// 이 인터페이스는 Event Grid Blob 이벤트의 data 속성 구조를 반영합니다.
interface BlobEventData {
    api: string;
    clientRequestId: string;
    requestId: string;
    eTag: string;
    contentType: string;
    contentLength: number;
    blobType: string;
    url: string;
    sequencer: string;
    storageDiagnostics: {
        batchId: string;
    };
}

// ffmpeg 실행 파일 경로 설정 (Azure Functions 환경에서 ffmpeg-static의 동작을 보장하기 위해 추가 설정 필요할 수 있음)
// 프로덕션 환경에서는 Custom Handler를 사용하여 ffmpeg를 직접 포함하는 것이 가장 안정적입니다.
ffmpeg.setFfmpegPath(ffmpegStatic!);

// --- 출력 바인딩 정의 ---
// 썸네일 이미지를 저장할 Blob 출력 바인딩
const thumbnails = output.storageBlob({
    path: "outcontents/{userId}/thumbnails/{filename}.jpg",
    connection: "AzureWebJobsStorage",
});

// 원본 이미지/비디오 프레임을 저장할 Blob 출력 바인딩
const origin = output.storageBlob({
    path: "outcontents/{userId}/origin/{filename}",
    connection: "AzureWebJobsStorage",
});

// 지원하지 않는 파일 타입 또는 기타 처리된 파일을 저장할 Blob 출력 바인딩
const postContents = output.storageBlob({
    path: "outcontents/{userId}/{filename}",
    connection: "AzureWebJobsStorage",
});

// 입력 Blob을 삭제하기 위한 Blob 출력 바인딩 (path는 트리거와 동일)
const deleteInputBlobOutput = output.storageBlob({
    path: "inputcontents/{userId}/{filename}", // 이 경로는 Event Grid 구독 필터와 일치해야 함
    connection: "AzureWebJobsStorage",
});

/**
 * 비디오 스트림에서 특정 시간의 프레임을 추출하여 Buffer로 반환합니다.
 * @param inputStream 비디오 파일의 ReadableStream
 * @param tmpDir 임시 파일을 저장할 디렉토리 경로
 * @returns 추출된 프레임 이미지의 Buffer
 */
async function extractFrame(
    inputStream: Readable,
    tmpDir: string
): Promise<Buffer> {
    const outputPath = path.join(tmpDir, "frame.jpg");
    const timestamp = process.env.FFMPEG_TIMESTAMP_SEC || "0.5"; // 환경 변수 사용 또는 기본값 0.5초

    return new Promise<Buffer>((resolve, reject) => {
        // ffmpeg에 스트림을 직접 입력으로 전달
        ffmpeg(inputStream)
            .outputOptions([`-ss ${timestamp}`, "-frames:v 1"]) // 특정 시간의 단일 프레임 추출
            .output(outputPath)
            .on("end", async () => {
                try {
                    const buf = await fs.readFile(outputPath);
                    resolve(buf);
                } catch (e) {
                    reject(new Error(`프레임 파일 읽기 실패: ${e.message}`));
                }
            })
            .on("error", (err) => {
                reject(new Error(`FFmpeg 프레임 추출 실패: ${err.message}`));
            })
            .run();
    });
}

/**
 * Event Grid Blob 이벤트를 처리하는 핸들러 함수입니다.
 * Blob 내용을 다운로드하여 이미지/비디오 파일을 처리하고 썸네일을 생성합니다.
 * @param event Event Grid 이벤트 객체 (BlobEventData 포함)
 * @param context 함수 호출 컨텍스트
 */
export async function eventGridTrigger(
    event: EventGridEvent,
    context: InvocationContext
): Promise<void> {
    // event.data를 BlobEventData 타입으로 캐스팅하여 사용합니다.
    // 'Record<string, unknown>' -> 'unknown' -> 'BlobEventData'
    const eventData = event.data as unknown as BlobEventData; // 👈 이중 타입 변환 적용

    // --- Blob URL에서 정보 추출 ---
    const blobUrl = eventData.url; // 👈 eventData 사용
    context.log(`[Event Grid] Blob 이벤트 수신: ${blobUrl}`);

    // URL에서 컨테이너 이름, userId, filename 추출
    const urlParts = new URL(blobUrl);
    // pathname은 /<containerName>/<userId>/<filename> 형태일 것으로 예상
    const pathSegments = urlParts.pathname.split("/").filter((s) => s); // 빈 문자열 제거

    if (pathSegments.length < 2) {
        context.error(`[Event Grid] 유효하지 않은 Blob 경로 형식: ${blobUrl}`);
        // Event Grid 구독 필터와 일치하지 않는 이벤트는 처리하지 않음
        return;
    }

    const containerName = pathSegments[0]; // 예: inputcontents
    const userId = pathSegments[1]; // 예: test
    const filename = pathSegments.slice(2).join("/"); // 예: download.png (하위 경로 포함 가능)
    const ext = filename.split(".").pop()?.toLowerCase();

    context.log(`[${userId}/${filename}] 처리 시작.`);

    const imageExt = ["jpg", "jpeg", "png", "gif", "bmp", "webp"];
    const videoExt = ["mp4", "mov", "avi", "mkv", "wmv"];
    const isImage = ext && imageExt.includes(ext);
    const isVideo = ext && videoExt.includes(ext);

    let tmpdir: string | undefined;

    try {
        // --- Blob 내용 다운로드 ---
        const connectionString = process.env.AzureWebJobsStorage;
        if (!connectionString) {
            context.error("AzureWebJobsStorage 연결 문자열을 찾을 수 없습니다.");
            throw new Error("AzureWebJobsStorage 연결 문자열이 설정되지 않았습니다.");
        }
        const blobServiceClient =
            BlobServiceClient.fromConnectionString(connectionString);
        const sourceContainerClient =
            blobServiceClient.getContainerClient(containerName);
        const sourceBlobClient = sourceContainerClient.getBlobClient(
            `${userId}/${filename}`
        ); // Blob 경로 재구성

        // Blob이 존재하는지 확인 (이벤트가 삭제 이벤트일 수도 있으므로)
        if (!(await sourceBlobClient.exists())) {
            context.log(
                `[${userId}/${filename}] Blob이 존재하지 않습니다. (삭제되었거나 다른 이벤트) 처리 건너뜀.`
            );
            return;
        }

        // Blob 내용을 Buffer로 다운로드
        const fullInputBuffer = await sourceBlobClient.downloadToBuffer();
        context.log(
            `[${userId}/${filename}] 입력 Blob 크기: ${fullInputBuffer.length} bytes`
        );

        // --- 기존 처리 로직 (Blob 내용이 Buffer로 준비된 상태) ---
        if (!isImage && !isVideo) {
            context.log(
                `[${userId}/${filename}] 지원하지 않는 파일 형식: .${ext}. 원본 파일만 저장합니다.`
            );
            context.extraOutputs.set(postContents, fullInputBuffer); // 처리 없이 원본 파일을 postContents에 저장
            context.log(`[${userId}/${filename}] 원본 파일 저장 완료.`);
        } else {
            let bufferForThumb: Buffer;

            if (isImage) {
                bufferForThumb = fullInputBuffer;
                context.log(`[${userId}/${filename}] 이미지 파일 감지.`);
            } else {
                // isVideo
                context.log(
                    `[${userId}/${filename}] 비디오 파일 감지. 첫 프레임 추출 시작.`
                );
                tmpdir = await fs.mkdtemp(path.join(os.tmpdir(), "thumb-"));

                const videoStreamForFFmpeg = new Readable();
                videoStreamForFFmpeg.push(fullInputBuffer);
                videoStreamForFFmpeg.push(null);

                bufferForThumb = await extractFrame(videoStreamForFFmpeg, tmpdir);
                context.log(`[${userId}/${filename}] 프레임 추출 완료.`);
            }

            const thumbnail = await sharp(bufferForThumb)
                .resize(200, 200, { fit: "inside", withoutEnlargement: true })
                .jpeg({ quality: 80 })
                .toBuffer();

            context.extraOutputs.set(thumbnails, thumbnail);
            context.extraOutputs.set(origin, bufferForThumb);
            context.log(`[${userId}/${filename}] 썸네일 및 원본/프레임 저장 완료.`);
        }

        // 처리 완료 후 입력 Blob 삭제 (출력 바인딩을 통해 null을 설정하여 삭제)
        context.extraOutputs.set(deleteInputBlobOutput, null);
        context.log(`[${userId}/${filename}] 원본 Blob 삭제 요청 완료.`);
    } catch (err) {
        context.error(`[${userId}/${filename}] 처리 중 오류 발생:`, err);
        // 오류 발생 시에도 임시 파일 정리 및 원본 Blob 삭제를 시도할 수 있도록 finally 블록 활용
        context.extraOutputs.set(deleteInputBlobOutput, null); // 실패해도 삭제 시도
        throw err; // 함수 실행 실패를 알리기 위해 오류 다시 던지기
    } finally {
        if (tmpdir) {
            try {
                await fs.rm(tmpdir, { recursive: true, force: true });
                context.log(
                    `[${userId}/${filename}] 임시 디렉토리 정리 완료: ${tmpdir}`
                );
            } catch (cleanupErr) {
                context.error(
                    `[${userId}/${filename}] 임시 디렉토리 정리 실패 ${tmpdir}:`,
                    cleanupErr
                );
            }
        }
    }
}

// --- Azure Function 정의 ---
// Event Grid 트리거로 변경: Event Grid 구독을 통해 이벤트를 받음
app.eventGrid("eventGridTrigger", {
    handler: eventGridTrigger, // Event Grid 핸들러 함수
    extraOutputs: [thumbnails, origin, postContents, deleteInputBlobOutput], // 추가 출력 바인딩들
});

import { app, InvocationContext, output } from "@azure/functions";
import sharp from "sharp";
import ffmpeg from "fluent-ffmpeg";
import ffmpegStatic from "ffmpeg-static";
import * as os from "os";
import * as path from "path";
import * as fs from "fs/promises";
import { Readable } from "stream"; // ReadableStream을 다루기 위해 필요

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
  path: "inputcontents/{userId}/{filename}",
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
 * Blob Storage에 파일이 업로드될 때 트리거되는 핸들러 함수입니다.
 * 이미지/비디오 파일을 처리하여 썸네일 및 원본/처리된 파일을 다른 Blob에 저장합니다.
 * @param inputBlob 트리거된 Blob의 ReadableStream
 * @param context 함수 호출 컨텍스트
 */
async function blobHandler(
  inputBlob: Readable, // Blob 입력은 ReadableStream으로 받습니다.
  context: InvocationContext
): Promise<void> {
  const userId = context.triggerMetadata.userId as string;
  const filename = context.triggerMetadata.filename as string;
  const ext = filename.split(".").pop()?.toLowerCase();

  context.log(`[${userId}/${filename}] 처리 시작.`);

  const imageExt = ["jpg", "jpeg", "png", "gif", "bmp", "webp"];
  const videoExt = ["mp4", "mov", "avi", "mkv", "wmv"];
  const isImage = ext && imageExt.includes(ext);
  const isVideo = ext && videoExt.includes(ext);

  // 임시 디렉토리 경로 (finally 블록에서 정리하기 위해 try 블록 밖에서 선언)
  let tmpdir: string | undefined;

  try {
    // 입력 스트림을 버퍼로 변환 (sharp 또는 ffmpeg에 전달하기 위해 필요)
    // 대용량 파일의 경우 이 과정에서 메모리 문제가 발생할 수 있습니다.
    // ffmpeg는 스트림을 직접 받을 수 있으나, sharp는 버퍼가 필요합니다.
    // 더 큰 최적화가 필요하다면, 파일을 임시 저장소에 먼저 쓴 후 처리하는 방식을 고려해야 합니다.
    const chunks: Buffer[] = [];
    for await (const chunk of inputBlob) {
      chunks.push(chunk);
    }
    const fullInputBuffer = Buffer.concat(chunks);
    context.log(
      `[${userId}/${filename}] 입력 Blob 크기: ${fullInputBuffer.length} bytes`
    );

    if (!isImage && !isVideo) {
      context.log(
        `[${userId}/${filename}] 지원하지 않는 파일 형식: .${ext}. 원본 파일만 저장합니다.`
      );
      context.extraOutputs.set(postContents, fullInputBuffer); // 처리 없이 원본 파일을 postContents에 저장
      context.log(`[${userId}/${filename}] 원본 파일 저장 완료.`);
    } else {
      let bufferForThumb: Buffer; // 썸네일 생성에 사용될 버퍼 (이미지이거나 비디오 프레임)

      if (isImage) {
        bufferForThumb = fullInputBuffer;
        context.log(`[${userId}/${filename}] 이미지 파일 감지.`);
      } else {
        // isVideo
        context.log(
          `[${userId}/${filename}] 비디오 파일 감지. 첫 프레임 추출 시작.`
        );
        tmpdir = await fs.mkdtemp(path.join(os.tmpdir(), "thumb-")); // 임시 디렉토리 생성

        // 비디오 스트림을 ffmpeg에 전달하기 위해 새로운 ReadableStream 생성
        const videoStreamForFFmpeg = new Readable();
        videoStreamForFFmpeg.push(fullInputBuffer);
        videoStreamForFFmpeg.push(null); // 스트림 끝을 알림

        bufferForThumb = await extractFrame(videoStreamForFFmpeg, tmpdir);
        context.log(`[${userId}/${filename}] 프레임 추출 완료.`);
      }

      // 썸네일 생성 (sharp 라이브러리 사용)
      const thumbnail = await sharp(bufferForThumb)
        .resize(200, 200, { fit: "inside", withoutEnlargement: true }) // 원본이 작으면 확대하지 않음
        .jpeg({ quality: 80 }) // JPEG 형식으로 80% 품질
        .toBuffer();

      // 출력 바인딩에 데이터 설정
      context.extraOutputs.set(thumbnails, thumbnail); // 썸네일 저장
      context.extraOutputs.set(origin, bufferForThumb); // 원본 또는 추출된 프레임 저장
      context.log(`[${userId}/${filename}] 썸네일 및 원본/프레임 저장 완료.`);
    }

    // 처리 완료 후 입력 Blob 삭제 (출력 바인딩을 통해 null을 설정하여 삭제)
    context.extraOutputs.set(deleteInputBlobOutput, null);
    context.log(`[${userId}/${filename}] 원본 Blob 삭제 요청 완료.`);
  } catch (err) {
    context.error(`[${userId}/${filename}] 처리 중 오류 발생:`, err);
    // 오류 발생 시에도 임시 파일 정리 및 원본 Blob 삭제를 시도할 수 있도록 finally 블록 활용
    // 에러 발생 시 원본 Blob을 삭제할지 여부는 정책에 따라 달라질 수 있습니다.
    // 여기서는 오류 발생 시에도 삭제 요청을 합니다.
    context.extraOutputs.set(deleteInputBlobOutput, null);
    throw err; // 함수 실행 실패를 알리기 위해 오류 다시 던지기
  } finally {
    // 임시 디렉토리 정리 (성공/실패 여부와 상관없이 실행)
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
// 'inputcontents' 컨테이너에 Blob이 업로드되면 이 함수가 트리거됩니다.
app.storageBlob("thumbnailTrigger", {
  path: "inputcontents/{userId}/{filename}", // 트리거 경로 패턴
  connection: "AzureWebJobsStorage", // 스토리지 계정 연결 문자열
  handler: blobHandler, // 실제 로직을 처리할 핸들러 함수
  extraOutputs: [thumbnails, origin, postContents, deleteInputBlobOutput], // 추가 출력 바인딩들
});

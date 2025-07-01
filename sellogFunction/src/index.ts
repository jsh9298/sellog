// @azure/functions 패키지에서 app 객체를 임포트합니다.
// 이 app 객체는 Azure Functions 애플리케이션의 전역 인스턴스이며,
// 모든 트리거 및 바인딩 정의가 이 객체에 연결됩니다.
import { app } from "@azure/functions";

// './functions/storageBlobTrigger' 파일을 임포트합니다.
// 이 임포트 문은 해당 파일의 최상위 레벨 코드를 실행시켜,
// storageBlobTrigger.ts 파일 내에 정의된 Azure Functions(예: app.storageBlob(...))를
// 현재 app 인스턴스에 등록하는 역할을 합니다.
// 'No job functions found' 오류를 방지하기 위해 모든 함수 정의 파일은 여기에 임포트되어야 합니다.
import "./functions/storageBlobTrigger";

// Azure Functions 애플리케이션의 전역 설정을 구성합니다.
// enableHttpStream: true는 HTTP 트리거 함수에서 스트리밍 기능을 활성화하는 설정입니다.
// 현재 Blob 트리거 함수와 직접적인 관련은 없지만, HTTP 트리거 함수가 있을 경우 유용합니다.
app.setup({
  enableHttpStream: true,
});

// 이 파일 자체는 명시적으로 함수를 내보내지 않습니다.
// 대신, 임포트된 파일(storageBlobTrigger.ts) 내에서 함수가 app 객체에 등록됩니다.

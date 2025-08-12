package com.teamproject.sellog.common.responseUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

//gpt 사용, 로그 부분은 나중에 할거라 지움
@RestControllerAdvice // 모든 @ControllerAdvice를 자동으로 스캔합니다.
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        // 1. 커스텀 예외 처리 (BaseException을 상속하는 모든 예외)
        @ExceptionHandler(BaseException.class)
        public ResponseEntity<RestResponse<?>> handleBaseException(BaseException e) {
                // 커스텀 예외에서 ErrorCode를 가져와 RestResponse로 반환
                return new ResponseEntity<>(
                                RestResponse.error(e.getErrorCode(), e.getMessage()),
                                e.getErrorCode().getHttpStatus());
        }

        // 2. @Valid 또는 @Validated 어노테이션으로 인한 유효성 검증 실패 예외 처리
        // MethodArgumentNotValidException은 Spring의 기본 예외 처리자에서 처리되므로,
        // ResponseEntityExceptionHandler를 상속하여 override 하는 것이 좋습니다.
        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status,
                        WebRequest request) {
                // 여러 필드 에러 메시지를 조합하여 반환할 수 있습니다.
                String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                return new ResponseEntity<>(
                                RestResponse.error(ErrorCode.INVALID_INPUT_VALUE, errorMessage), // COMMON-006 사용
                                HttpStatus.BAD_REQUEST // HTTP Status는 Bad Request (400)
                );
        }

        // 3. IllegalArgumentException 등 일반적인 런타임 예외 처리
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<RestResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
                return new ResponseEntity<>(
                                RestResponse.error(ErrorCode.INVALID_PARAMETER, e.getMessage()), // COMMON-001 사용
                                HttpStatus.BAD_REQUEST);
        }

        // 4. 나머지 처리되지 않은 모든 예외 (최후의 보루)
        @ExceptionHandler(Exception.class)
        public ResponseEntity<RestResponse<?>> handleGlobalException(Exception e) {
                return new ResponseEntity<>(
                                RestResponse.error(ErrorCode.INTERNAL_SERVER_ERROR), // COMMON-005 사용
                                HttpStatus.INTERNAL_SERVER_ERROR // HTTP Status는 Internal Server Error (500)
                );
        }

        // + @AuthenticationException, @AccessDeniedException 등 시큐리티 관련 예외도 여기서 처리할 수
        // 있습니다.
}
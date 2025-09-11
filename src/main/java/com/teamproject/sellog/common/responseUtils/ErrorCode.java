package com.teamproject.sellog.common.responseUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

//gpt 딸깍
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "001", "요청 파라미터가 유효하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "003", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "004", "요청 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "005", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "006", "잘못된 입력 값입니다."),
    // 토큰
    INVALID_OR_EXPIRED_TOKEN(HttpStatus.FORBIDDEN, "101", "만료된 토큰입니다."),
    TOKEN_NOT_PROVIDED(HttpStatus.FORBIDDEN, "102", "토큰이 없습니다."),
    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "201", "해당 사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "202", "이미 존재하는 사용자 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "203", "비밀번호가 일치하지 않습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "204", "해당 사용자는 비활성 상태입니다."),
    INVALID_F_LIST_CONTROL(HttpStatus.BAD_REQUEST, "205", "잘못된 팔로우/블락 요청입니다."),
    ACCOUNT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "206", "해당아이디 소유자가 아닙니다."),
    FOLLOW_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, "207", "이미 팔로우를 요청한 사용자입니다."),
    FOLLOW_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "208", "존재하지 않는 팔로우 요청입니다."),

    // 게시글 관련
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "301", "해당 게시글을 찾을 수 없습니다."),
    POST_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "302", "게시글 작성자가 아닙니다."),
    POST_DENY_OWN_POST(HttpStatus.FORBIDDEN, "303", "작성자 본인은 할 수 없는 요청입니다."),
    POST_DENY_MULTIPLE(HttpStatus.FORBIDDEN, "304", "이미 처리된 요청입니다."),

    // 리뷰 관련
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "401", "해당 리뷰를 찾을 수 없습니다."),
    REVIEW_DENY_MULTIPLE(HttpStatus.FORBIDDEN, "402", "이미 리뷰를 작성한 게시글입니다."),
    REVIEW_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "403", "리뷰를 수정하거나 삭제할 권한이 없습니다."),
    CANNOT_REVIEW_OWN_POST(HttpStatus.BAD_REQUEST, "404", "자신이 작성한 판매 게시글에는 리뷰를 남길 수 없습니다."),
    CANNOT_REVIEW_NON_PRODUCT(HttpStatus.BAD_REQUEST, "405", "판매 게시글에만 리뷰를 남길 수 있습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "406", "이미 해당 게시글에 리뷰를 작성했습니다."),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "501", "해당 댓글을 찾을 수 없습니다."),
    COMMENT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "502", "댓글을 수정하거나 삭제할 권한이 없습니다."),
    COMMENT_IS_DELETED(HttpStatus.BAD_REQUEST, "503", "삭제된 댓글에는 답글을 달 수 없습니다."),

    // OTP
    OTP_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "601", "OTP 생성에 실패했습니다."),
    OTP_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "602", "OTP가 일치하지 않거나 만료되었습니다."),
    USER_EMAIL_MISMATCH(HttpStatus.BAD_REQUEST, "603", "사용자 정보가 일치하지 않습니다."),
    OTP_NOT_VERIFIED(HttpStatus.FORBIDDEN, "604", "OTP 인증이 완료되지 않았습니다."),
    // ErrorCode.java 파일에 추가
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "007", "허용되지 않은 요청 메서드입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "008", "파라미터 타입이 유효하지 않습니다.");

    private final HttpStatus httpStatus; // HTTP 상태 코드 (예: 400 Bad Request, 404 Not Found)
    private final String code; // 애플리케이션 내부에서 사용할 커스텀 에러 코드 (예: USER-001)
    private final String message; // 개발자 또는 사용자에게 보여줄 에러 메시지
}
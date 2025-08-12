package com.teamproject.sellog.common.responseUtils;

import lombok.Getter;

//gpt 딸깍
@Getter
public class BaseException extends RuntimeException { // RuntimeException을 상속하여 Unchecked Exception으로 만듭니다.
    private final ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 부모 클래스에 ErrorCode의 메시지를 전달
        this.errorCode = errorCode;
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
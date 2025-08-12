package com.teamproject.sellog.common.responseUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public final class RestResponse<T> {
    private final Boolean isSuccess;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T payload;

    // 성공 응답을 위한 팩토리 메서드 --gpt 수정
    public static <T> RestResponse<T> success(T payload) {
        return new RestResponse<>(true, "200", "Success", payload);
    }

    public static <T> RestResponse<T> success(String message, T payload) {
        return new RestResponse<>(true, "200", message, payload);
    }

    // 오류 응답을 위한 팩토리 메서드 (payload는 null)
    public static <T> RestResponse<T> error(ErrorCode errorCode) {
        return new RestResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> RestResponse<T> error(ErrorCode errorCode, String message) {
        return new RestResponse<>(false, errorCode.getCode(), message, null);
    }
}

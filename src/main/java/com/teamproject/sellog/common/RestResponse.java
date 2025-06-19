package com.teamproject.sellog.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public final class RestResponse<T> {
    private final Boolean isSuccess;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T payload;

    @JsonCreator
    public RestResponse(Boolean isSuccess, String code, String message, T payload) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.payload = payload;
    }
}

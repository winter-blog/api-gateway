package com.devwinter.apigateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private String result;
    private String message;
    private T body;

    @Getter
    @RequiredArgsConstructor
    public enum ResultCode {
        SUCCESS("success"),
        FAIL("fail")
        ;

        private final String description;
    }

    public static <T> BaseResponse<T> success(T body) {
        return new BaseResponse<>(ResultCode.SUCCESS.description, null, body);
    }

    public static <T> BaseResponse<T> error(String message, T body) {
        return new BaseResponse<>(ResultCode.FAIL.description, message, body);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(ResultCode.FAIL.description, message, null);
    }
}

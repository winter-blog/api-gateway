package com.devwinter.apigateway.response;

import com.devwinter.apigateway.exception.ApiGatewayErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private Result result;
    private T body;

    @Getter
    @RequiredArgsConstructor
    public enum ResultCode {
        SUCCESS("success"),
        FAIL("fail")
        ;

        private final String description;
    }

    @Getter
    @Builder(access = AccessLevel.PROTECTED)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private String status;
        private String message;

        public static Result success(String message) {
            return Result.builder()
                         .status(ResultStatus.SUCCESS.description)
                         .message(message)
                         .build();
        }

        public static Result fail(String message) {
            return Result.builder()
                         .status(ResultStatus.FAIL.description)
                         .message(message)
                         .build();
        }

        @Getter
        @RequiredArgsConstructor
        public enum ResultStatus {
            SUCCESS("success"),
            FAIL("fail")
            ;

            private final String description;
        }
    }

    public static <T> BaseResponse<T> error(ApiGatewayErrorCode errorCode) {
        return new BaseResponse<>(Result.fail(errorCode.getMessage()), null);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(Result.fail(message), null);
    }
}

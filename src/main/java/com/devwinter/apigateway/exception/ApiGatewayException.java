package com.devwinter.apigateway.exception;

import lombok.Getter;

@Getter
public class ApiGatewayException extends RuntimeException {

    private final ApiGatewayErrorCode apiGatewayErrorCode;

    public ApiGatewayException(ApiGatewayErrorCode errorCode) {
        super(errorCode.getMessage());
        this.apiGatewayErrorCode = errorCode;
    }
}

package com.devwinter.apigateway.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
@ToString
public enum ApiGatewayErrorCode {

    AUTHORIZATION_HEADER_NOT_EXIST(UNAUTHORIZED, "Header에 Authorization값이 존재하지 않습니다."),
    USER_ID_HEADER_NOT_EXIST(UNAUTHORIZED, "Header에 User-Id값이 존재하지 않습니다."),
    JWT_TOKEN_VALID_FAIL(UNAUTHORIZED, "JWT토큰 유효성 검사에 실패하였습니다."),
    JWT_TOKEN_EXPIRE(UNAUTHORIZED, "JWT 토큰 유효기간이 만료되었습니다. 재발급 요청을 해주세요."),
    JWT_TOKEN_AND_USER_ID_NOT_VALID(UNAUTHORIZED, "JWT토큰에 User-Id와 User-Id값이 일치하지 않습니다."),
    AUTH_SERVER_NOT_RESPONSE(INTERNAL_SERVER_ERROR, "인증 서버가 응답이 없습니다."),
    USER_NOT_FOUND(NOT_FOUND, "회원 정보가 없습니다.")
    ;

    private final HttpStatus code;
    private final String message;
}

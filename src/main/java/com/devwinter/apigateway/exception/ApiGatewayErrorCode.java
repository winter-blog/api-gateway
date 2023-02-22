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
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),
    REQUEST_SERVICE_WAIT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청하신 서버가 로딩중입니다. 잠시만 기다려주세요."),
    AUTHORIZATION_HEADER_NOT_EXIST(UNAUTHORIZED, "Header에 Authorization값이 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_EXIST(UNAUTHORIZED, "refresh token이 존재하지 않습니다."),
    USER_ID_HEADER_NOT_EXIST(UNAUTHORIZED, "Header에 MemberId값이 존재하지 않습니다."),
    ACCESS_TOKEN_VALID_FAIL(UNAUTHORIZED, "AccessToken 유효성 검사에 실패하였습니다."),
    REFRESH_TOKEN_VALID_FAIL(UNAUTHORIZED, "RefreshToken 유효성 검사에 실패하였습니다."),
    JWT_TOKEN_EXPIRE(UNAUTHORIZED, "JWT 토큰 유효기간이 만료되었습니다. 재발급 요청을 해주세요."),
    JWT_TOKEN_AND_USER_ID_NOT_VALID(UNAUTHORIZED, "JWT토큰에 User-Id와 User-Id값이 일치하지 않습니다."),
    AUTH_SERVER_NOT_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "인증 서버가 응답이 없습니다."),
    USER_NOT_VALID_EXCEPTION(NOT_FOUND, "회원 인증에 실패하였습니다."),
    JSON_PROCESSING_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 처리 중 에러가 발생하였습니다.")
    ;

    private final HttpStatus code;
    private final String message;
}

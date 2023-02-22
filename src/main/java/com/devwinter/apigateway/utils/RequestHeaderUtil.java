package com.devwinter.apigateway.utils;

import com.devwinter.apigateway.exception.ApiGatewayException;
import org.apache.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;
import java.util.Objects;

import static com.devwinter.apigateway.exception.ApiGatewayErrorCode.*;

public class RequestHeaderUtil {
    public static Long getMemberId(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey("MemberId")) {
            throw new ApiGatewayException(USER_ID_HEADER_NOT_EXIST);
        }

        List<String> userId = request.getHeaders().get("MemberId");

        if (Objects.isNull(userId)) {
            throw new ApiGatewayException(USER_ID_HEADER_NOT_EXIST);
        }
        return Long.valueOf(userId.get(0));
    }

    public static String getAccessToken(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            throw new ApiGatewayException(AUTHORIZATION_HEADER_NOT_EXIST);
        }

        List<String> authorizations = request.getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (Objects.isNull(authorizations)) {
            throw new ApiGatewayException(AUTHORIZATION_HEADER_NOT_EXIST);
        }

        String authorizationHeader = authorizations.get(0);
        return authorizationHeader.replace("Bearer", "");
    }

    public static String getRefreshToken(ServerHttpRequest request) {
        if (!request.getHeaders().containsKey("RefreshToken")) {
            throw new ApiGatewayException(REFRESH_TOKEN_NOT_EXIST);
        }

        List<String> authorizations = request.getHeaders().get("RefreshToken");

        if (Objects.isNull(authorizations)) {
            throw new ApiGatewayException(REFRESH_TOKEN_NOT_EXIST);
        }

        return authorizations.get(0);
    }
}

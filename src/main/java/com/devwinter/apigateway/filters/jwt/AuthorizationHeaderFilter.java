package com.devwinter.apigateway.filters.jwt;

import com.devwinter.apigateway.exception.ApiGatewayErrorCode;
import com.devwinter.apigateway.exception.ApiGatewayException;
import com.devwinter.apigateway.response.BaseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static com.devwinter.apigateway.exception.ApiGatewayErrorCode.*;

@Slf4j
@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public AuthorizationHeaderFilter(
            JwtTokenProvider jwtTokenProvider,
            ObjectMapper objectMapper
    ) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            try {
                Long userId = getUserId(request);
                String accessToken = getAccessToken(request);

                // 토큰 유효성 검사 및 유효시간 확인
                tokenValidAndExpireCheck(accessToken);

                // 토큰 위변조 검증
                tokenForgeryCheck(accessToken, userId);

                // 회원이 있는지 검사
                Mono<BaseResponse<Boolean>> baseResponseMono = credential(userId);

                return baseResponseMono
                        .flatMap(res -> {
                            if (res.getResult().equals("success") && res.getBody()) {
                                ServerHttpRequest requestChange = addHeaderEmail(exchange, accessToken);
                                return chain.filter(exchange.mutate()
                                                            .request(requestChange)
                                                            .build());
                            } else {
                                return onError(exchange, USER_NOT_FOUND);
                            }
                        });
            } catch (ApiGatewayException e) {
                return onError(exchange, e.getApiGatewayErrorCode());
            }
        };
    }

    private Long getUserId(ServerHttpRequest request) {
        if (!request.getHeaders()
                    .containsKey("User-Id")) {
            throw new ApiGatewayException(USER_ID_HEADER_NOT_EXIST);
        }

        List<String> userId = request.getHeaders()
                                     .get("User-Id");

        if (Objects.isNull(userId)) {
            throw new ApiGatewayException(USER_ID_HEADER_NOT_EXIST);
        }
        return Long.valueOf(userId.get(0));
    }

    private String getAccessToken(ServerHttpRequest request) {
        if (!request.getHeaders()
                    .containsKey(HttpHeaders.AUTHORIZATION)) {
            throw new ApiGatewayException(AUTHORIZATION_HEADER_NOT_EXIST);
        }

        List<String> authorizations = request.getHeaders()
                                             .get(HttpHeaders.AUTHORIZATION);
        if (Objects.isNull(authorizations)) {
            throw new ApiGatewayException(AUTHORIZATION_HEADER_NOT_EXIST);
        }

        String authorizationHeader = authorizations.get(0);
        return authorizationHeader.replace("Bearer", "");
    }

    private void tokenValidAndExpireCheck(String accessToken) {
        try {
            jwtTokenProvider.tokenValid(accessToken);
        } catch(ExpiredJwtException e) {
            throw new ApiGatewayException(JWT_TOKEN_EXPIRE);
        } catch (Exception e) {
            throw new ApiGatewayException(JWT_TOKEN_VALID_FAIL);
        }
    }

    private void tokenForgeryCheck(String accessToken, Long userId) {
        if (!userId.equals(jwtTokenProvider.getAudienceFromToken(accessToken))) {
            throw new ApiGatewayException(JWT_TOKEN_AND_USER_ID_NOT_VALID);
        }
    }

    private Mono<BaseResponse<Boolean>> credential(Long userId) {
        WebClient webClient = WebClient.builder()
                                       .baseUrl("http://localhost:8080/auth-service")
                                       .build();
        return webClient.get()
                        .uri("/" + userId + "/valid")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<>() {
                        });
    }

    private ServerHttpRequest addHeaderEmail(ServerWebExchange exchange, String accessToken) {
        return exchange.getRequest()
                       .mutate()
                       .header("email", jwtTokenProvider.getClaimFromToken(accessToken, Claims::getSubject))
                       .build();
    }

    public Mono<Void> onError(ServerWebExchange exchange, ApiGatewayErrorCode errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(errorCode.getCode());

        try {
            BaseResponse<Object> errorResponse = BaseResponse.error(errorCode.getMessage());
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(bytes);
            log.error("AuthorizationHeaderFilter Error: {}", errorCode);
            return exchange.getResponse()
                           .writeWith(Flux.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("AuthorizationHeaderFilter JsonProcessingException: ", e);
            return response.setComplete();
        }
    }

    public static class Config {

    }
}

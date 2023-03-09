package com.devwinter.apigateway.exception;

import com.devwinter.apigateway.response.BaseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-1)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        byte[] bytes = null;
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        if(ex instanceof NotFoundException) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            bytes = getBytes(BaseResponse.error(((NotFoundException) ex).getReason()));
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiGatewayException apiGatewayException = null;
        if (ex instanceof ApiGatewayException) {
            apiGatewayException = ((ApiGatewayException) ex);
            response.setStatusCode(apiGatewayException.getApiGatewayErrorCode().getCode());
        }

        if (apiGatewayException != null) {
            ApiGatewayErrorCode errorCode = apiGatewayException.getApiGatewayErrorCode();
            BaseResponse<Object> errorResponse = BaseResponse.error(errorCode.getMessage());
            bytes = getBytes(errorResponse);
            log.error("AuthorizationHeaderFilter Error: {}", errorCode);
        } else {
            if(bytes == null) {
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                bytes = getBytes(BaseResponse.error(ApiGatewayErrorCode.INTERNAL_SERVER_ERROR));
            }
        }

        if (bytes != null) {
            DataBuffer buffer = exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(bytes);
            return exchange.getResponse()
                           .writeWith(Flux.just(buffer));
        } else {
            return Mono.error(ex);
        }
    }

    private byte[] getBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException : " + e.getMessage());
            return null;
        }
    }
}
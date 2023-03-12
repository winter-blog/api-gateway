package com.devwinter.apigateway.filters;

import com.devwinter.apigateway.exception.ApiGatewayException;
import com.devwinter.apigateway.response.BaseResponse;
import com.devwinter.apigateway.application.service.AccessTokenValidService;
import com.devwinter.apigateway.response.MemberValidResponse;
import com.devwinter.apigateway.utils.RequestHeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.devwinter.apigateway.exception.ApiGatewayErrorCode.USER_NOT_VALID_EXCEPTION;
import static com.devwinter.apigateway.response.BaseResponse.Result.ResultStatus.SUCCESS;


@Slf4j
@Component
public class AccessTokenAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AccessTokenAuthorizationHeaderFilter.Config> {

    private final AccessTokenValidService accessTokenValidService;

    public AccessTokenAuthorizationHeaderFilter(
            AccessTokenValidService accessTokenValidService
    ) {
        super(Config.class);
        this.accessTokenValidService = accessTokenValidService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("request:{}", request.getPath());

            Long memberId = RequestHeaderUtil.getMemberId(request);
            String accessToken = RequestHeaderUtil.getAccessToken(request);

            Mono<BaseResponse<MemberValidResponse>> response = accessTokenValidService.valid(memberId, accessToken);

            return response
                    .flatMap(res -> {
                        if(res.getResult().getStatus().equals(SUCCESS.getDescription()) &&
                                Objects.nonNull(res.getBody()) &&
                                res.getBody().getMemberId().equals(memberId)) {
                            MemberValidResponse validResponse = res.getBody();
                            ServerHttpRequest requestChange = addRequestHeaderEmail(exchange, validResponse.getEmail());
                            return chain.filter(exchange.mutate().request(requestChange).build());
                        } else {
                            return Mono.error(new ApiGatewayException(USER_NOT_VALID_EXCEPTION));
                        }
                    });

        };
    }

    private ServerHttpRequest addRequestHeaderEmail(ServerWebExchange exchange, String email) {
        return exchange.getRequest()
                       .mutate()
                       .header("Email", email)
                       .build();
    }

    public static class Config {

    }
}

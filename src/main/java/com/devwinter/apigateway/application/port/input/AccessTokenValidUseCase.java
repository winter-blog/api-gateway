package com.devwinter.apigateway.application.port.input;

import com.devwinter.apigateway.response.BaseResponse;
import com.devwinter.apigateway.response.MemberValidResponse;
import reactor.core.publisher.Mono;

public interface AccessTokenValidUseCase {
    Mono<BaseResponse<MemberValidResponse>> valid(Long userId, String accessToken);
}

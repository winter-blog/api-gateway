package com.devwinter.apigateway.application.port.output;

import com.devwinter.apigateway.response.BaseResponse;
import com.devwinter.apigateway.response.MemberValidResponse;
import reactor.core.publisher.Mono;

public interface GetUserValidPort {
    Mono<BaseResponse<MemberValidResponse>> requestValidMemberCheck(String email);
}

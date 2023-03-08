package com.devwinter.apigateway.application.service;

import com.devwinter.apigateway.application.port.input.AccessTokenValidUseCase;
import com.devwinter.apigateway.application.port.output.GetUserValidPort;
import com.devwinter.apigateway.response.BaseResponse;
import com.devwinter.apigateway.response.MemberValidResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccessTokenValidService implements AccessTokenValidUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final GetUserValidPort getUserValidPort;

    @Override
    public Mono<BaseResponse<MemberValidResponse>> valid(Long userId, String accessToken) {
        // 토큰 유효성 검사 및 유효시간 확인
        jwtTokenProvider.accessTokenValidAndExpireCheck(accessToken);

        // 토큰 위변조 검증
        jwtTokenProvider.tokenForgeryCheck(userId, accessToken);

        return getUserValidPort.requestValidMemberCheck(jwtTokenProvider.getSubjectClaim(accessToken));
    }
}

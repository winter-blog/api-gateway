package com.devwinter.apigateway.application.service;

import com.devwinter.apigateway.application.port.input.RefreshTokenValidUseCase;
import com.devwinter.apigateway.application.port.output.GetUserValidPort;
import com.devwinter.apigateway.response.BaseResponse;
import com.devwinter.apigateway.response.MemberValidResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.devwinter.apigateway.application.service.JwtTokenProvider.JwtTokenType.ACCESS_TOKEN;
import static com.devwinter.apigateway.application.service.JwtTokenProvider.JwtTokenType.REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class RefreshTokenValidService implements RefreshTokenValidUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final GetUserValidPort getUserValidPort;

    @Override
    public Mono<BaseResponse<MemberValidResponse>> valid(Long userId, String accessToken, String refreshToken) {

        // access token 유효성 검사
        jwtTokenProvider.tokenValid(accessToken, ACCESS_TOKEN);

        // refresh token 유효성 검사
        jwtTokenProvider.tokenValid(refreshToken, REFRESH_TOKEN);

        // 토큰 위변조 검증
        jwtTokenProvider.tokenForgeryCheck(userId, accessToken);

        return getUserValidPort.requestValidUserCheck(userId);
    }
}

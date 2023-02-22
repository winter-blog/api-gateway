package com.devwinter.apigateway.adapter.output.webclient;

import com.devwinter.apigateway.application.port.output.GetUserValidPort;
import com.devwinter.apigateway.response.BaseResponse;
import com.devwinter.apigateway.response.MemberValidResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RequestAuthValidAdapter implements GetUserValidPort {

    @Override
    public Mono<BaseResponse<MemberValidResponse>> requestValidUserCheck(Long memberId) {
        WebClient webClient = WebClient.builder()
                                       .baseUrl("http://localhost:8080/auth-service")
                                       .build();
        return webClient.get()
                        .uri("/" + memberId + "/valid")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<>() {
                        });
    }
}

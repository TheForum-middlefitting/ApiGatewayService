package com.example.apigatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(CustomFilter.Config config) {
        //Custom Pre Filter
            //exchang와 chain 객체 두개를 받는다.
        return (exchange, chain) -> {
            //import org.springframework.http.server.reactive.ServerHttpRequest;
            //import org.springframework.http.server.reactive.ServerHttpResponse;
            //exchage 부터 request, response를 얻을 수 있다.
            //pre에 먼저 할 동작을 정의한다.
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            log.info("Custom PRE filter: request id -> {}", request.getId());
            //Custom Post Filter
                //반환되는 체인에 연결시켜서
                //post에 할 동작 //추후에 사용자 로그인 기능을 해당 필터에서 처리한다.
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("Custom POST filter: response code -> {}", response.getStatusCode());
            }));
        };
    }


    public static class Config {
        //put the configurration properties

    }
}

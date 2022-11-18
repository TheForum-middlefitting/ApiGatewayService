package com.example.apigatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(LoggingFilter.Config config) {
        //반환하고자 하는 것은 GatewayFilter, apply함수를 진행
        //Ordered는 gateway를 생성해주는 자식 클래스
            // 생성자를 구현하고, order순서를 받는다. filter 메서드를 정의하는데, ServerWebExchang, GatewayFilterChain을 인자로 받는다.
            // Spring에서 MVC 패턴을 이용하여 받았는데, flux는 server res req를 사용한다.
                //그것을 사용하도록 도와주는 것이 exchange 이다.
            //GatewayFilterChain의 역할은 체인을 연결시켜 작업할 수 있게 도와주는 것이다.
        GatewayFilter filter = new OrderedGatewayFilter((exchange, chain) -> {
            //반환하고자 하는 것은 GatewayFilter, apply함수를 진행
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Logging Filter: baseMessage: {}", config.getBaseMessage());
            if (config.isPreLogger()) {
                log.info("Logging Pre Filter: request id -> {}", request.getId());
            }
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {
                    log.info("Logging POST Filter: response code -> {}", response.getStatusCode());
                }
            }));
        }, Ordered.LOWEST_PRECEDENCE);
        return filter;
    }


    //Getter Setter를 위해 어노테이션 추가
    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}

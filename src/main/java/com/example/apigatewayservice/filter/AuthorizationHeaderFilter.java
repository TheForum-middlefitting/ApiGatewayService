package com.example.apigatewayservice.filter;

import com.auth0.jwt.exceptions.*;
import com.example.apigatewayservice.config.jwt.JwtProperties;
import com.example.apigatewayservice.utils.errorCode.ErrorCode;
import com.example.apigatewayservice.utils.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    public static class Config {

    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();
                JwtUtils.verifyJwtToken(request, JwtProperties.ACCESS_HEADER_STRING);
                return  chain.filter(exchange);
            } catch (JWTDecodeException e) {
                return onError(exchange, ErrorCode.JWTDecodeFailed.getCode(), ErrorCode.JWTDecodeFailed.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (TokenExpiredException e) {
                return onError(exchange, ErrorCode.TokenExpired.getCode(), ErrorCode.TokenExpired.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (InvalidClaimException e) {
                return onError(exchange, ErrorCode.InvalidClaim.getCode(), ErrorCode.InvalidClaim.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (SignatureVerificationException e) {
                return onError(exchange, ErrorCode.SignatureVerificationFailed.getCode(), ErrorCode.SignatureVerificationFailed.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (JWTVerificationException e) {
                return onError(exchange, ErrorCode.JWTVerificationFailed.getCode(), ErrorCode.JWTVerificationFailed.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (IllegalArgumentException e) {
                return onError(exchange, ErrorCode.NoToken.getCode(), ErrorCode.NoToken.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (RuntimeException e) {
                return onError(exchange, ErrorCode.Forbidden.getCode(), ErrorCode.Forbidden.getMessage(), HttpStatus.FORBIDDEN);
            } catch (Exception e) {
                return onError(exchange, ErrorCode.InternalServer.getCode(), ErrorCode.InternalServer.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, int code, String message, HttpStatus status) {
        ObjectMapper objectMapper = new ObjectMapper();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        byte[] bytes = errorCodeMaker(String.valueOf(code), message, status.value()).getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(dataBuffer));
    }

    private String errorCodeMaker(String code, String message, int status) {
        return String.format("{\"code\" : \"%s\", \"message\" : \"%s\", \"status\" : %d}", code, message, status);
    }
}

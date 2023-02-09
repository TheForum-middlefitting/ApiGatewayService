package com.example.apigatewayservice.filter;

import com.auth0.jwt.exceptions.*;
import com.example.apigatewayservice.utils.errorCode.ErrorHelper;
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
    private final Environment env;
    private final JwtUtils jwtUtils;
    private final ErrorHelper errorHelper;

    public AuthorizationHeaderFilter(Environment env, JwtUtils jwtUtils, ErrorHelper errorHelper) {
        super(Config.class);
        this.env = env;
        this.jwtUtils = jwtUtils;
        this.errorHelper = errorHelper;
    }

    public static class Config { }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            try {
                ServerHttpRequest request = exchange.getRequest();
                jwtUtils.verifyJwtToken(request, env.getProperty("token.ACCESS_HEADER_STRING"));
                return  chain.filter(exchange);
            } catch (JWTDecodeException e) {
                return onError(exchange, errorHelper.getCode("JWTDecodeFailed"), errorHelper.getMsg("JWTDecodeFailed"), HttpStatus.UNAUTHORIZED);
            } catch (TokenExpiredException e) {
                return onError(exchange, errorHelper.getCode("TokenExpired"), errorHelper.getMsg("TokenExpired"), HttpStatus.UNAUTHORIZED);
            } catch (InvalidClaimException e) {
                return onError(exchange, errorHelper.getCode("InvalidClaim"), errorHelper.getMsg("InvalidClaim"), HttpStatus.UNAUTHORIZED);
            } catch (SignatureVerificationException e) {
                return onError(exchange, errorHelper.getCode("SignatureVerificationFailed"), errorHelper.getMsg("SignatureVerificationFailed"), HttpStatus.UNAUTHORIZED);
            } catch (JWTVerificationException e) {
                return onError(exchange, errorHelper.getCode("JWTVerificationFailed"), errorHelper.getMsg("JWTVerificationFailed"), HttpStatus.UNAUTHORIZED);
            } catch (IllegalArgumentException e) {
                return onError(exchange, errorHelper.getCode(e.getMessage()), errorHelper.getMsg(e.getMessage()), HttpStatus.UNAUTHORIZED);
            } catch (RuntimeException e) {
                return onError(exchange, errorHelper.getCode("Forbidden"), errorHelper.getMsg("Forbidden"), HttpStatus.FORBIDDEN);
            } catch (Exception e) {
                return onError(exchange, errorHelper.getCode("InternalServer"), errorHelper.getMsg("InternalServer"), HttpStatus.UNAUTHORIZED);
            }
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String code, String message, HttpStatus status) {
        ObjectMapper objectMapper = new ObjectMapper();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        byte[] bytes = errorCodeMaker(code, message, status.value()).getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(dataBuffer));
    }

    private String errorCodeMaker(String code, String message, int status) {
        return String.format("{\"code\" : \"%s\", \"message\" : \"%s\", \"status\" : %d}", code, message, status);
    }
}

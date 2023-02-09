package com.example.apigatewayservice.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.apigatewayservice.utils.cheak.CommonCheckUtil;
import com.example.apigatewayservice.utils.exception.AuthenticationFailedException;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class JwtUtils {
    private final Environment env;

    public JwtUtils(Environment env) {
        this.env = env;
    }

    public void verifyJwtTokenAndAuthority(ServerHttpRequest request, Long id, String tokenType) {
        Long verifyId = verifyJwtToken(request, tokenType);
        if (!verifyId.equals(id)) {
            throw new AuthenticationFailedException(env.getProperty("token.AuthFailed"));
        }
    }

    public Long verifyJwtToken(ServerHttpRequest request, String tokenType) {
        CommonCheckUtil.booleanCheck404(request.getHeaders().containsKey(tokenType), env.getProperty("token.NoToken"));
        String authorizationHeader = Objects.requireNonNull(request.getHeaders().get(tokenType)).get(0);
        String jwtToken = authorizationHeader.replace(Objects.requireNonNull(env.getProperty("token.TOKEN_PREFIX")), "");
        DecodedJWT verify = JWT.require(Algorithm.HMAC512(Objects.requireNonNull(tokenType
                        .equals(env.getProperty("token.ACCESS_HEADER_STRING"))
                        ? env.getProperty("token.ACCESS_SECRET")
                        : env.getProperty("token.REFRESH_SECRET"))))
                        .build().verify(jwtToken);
        return verify.getClaim("id").asLong();
    }

    public void sameTokenMemberCheck(String accessToken, String refreshToken) {
        String accessId = JWT.decode(accessToken).getClaim("id").toString();
        String refreshId = JWT.decode(refreshToken).getClaim("id").toString();
        if (!accessId.equals(refreshId)) {
            throw new RuntimeException();
        }
    }

}

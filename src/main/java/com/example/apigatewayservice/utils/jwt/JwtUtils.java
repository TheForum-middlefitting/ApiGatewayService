package com.example.apigatewayservice.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.apigatewayservice.config.jwt.JwtProperties;
import com.example.apigatewayservice.utils.cheak.CommonCheckUtil;
import com.example.apigatewayservice.utils.exception.AuthenticationFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import static com.example.apigatewayservice.utils.errorCode.ErrorCode.NoToken;


public class JwtUtils {
    private JwtUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    public static void verifyJwtTokenAndAuthority(ServerHttpRequest request, Long id, String tokenType) {
        Long verifyId = verifyJwtToken(request, tokenType);
        if (!verifyId.equals(id)) {
            throw new AuthenticationFailedException("AuthFailed");
        }
    }

    public static Long verifyJwtToken(ServerHttpRequest request, String tokenType) {
        CommonCheckUtil.booleanCheck404(request.getHeaders().containsKey(tokenType), NoToken.toString());
        String authorizationHeader = request.getHeaders().get(tokenType).get(0);
        String jwtToken = authorizationHeader.replace(JwtProperties.TOKEN_PREFIX, "");
        DecodedJWT verify = JWT.require(Algorithm.HMAC512(tokenType.equals(JwtProperties.ACCESS_HEADER_STRING) ? JwtProperties.Access_SECRET : JwtProperties.Refresh_SECRET)).build().verify(jwtToken);
        return verify.getClaim("id").asLong();
    }

    public static void sameTokenMemberCheck(String accessToken, String refreshToken) {
        String accessId = JWT.decode(accessToken).getClaim("id").toString();
        String refreshId = JWT.decode(refreshToken).getClaim("id").toString();
        if (!accessId.equals(refreshId)) {
            throw new RuntimeException();
        }
    }

}

package com.devwinter.apigateway.application.service;

import com.devwinter.apigateway.exception.ApiGatewayException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.function.Function;

import static com.devwinter.apigateway.application.service.JwtTokenProvider.JwtTokenType.ACCESS_TOKEN;
import static com.devwinter.apigateway.exception.ApiGatewayErrorCode.*;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key secretKey;

    public enum JwtTokenType {
        ACCESS_TOKEN,
        REFRESH_TOKEN
    }

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secret
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private Long getAudienceFromToken(String token) {
        return Long.valueOf(getClaimFromToken(token, Claims::getAudience));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public String getSubjectClaim(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(secretKey)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    public void accessTokenValidAndExpireCheck(String token) {
        try {
            tokenValid(token, ACCESS_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new ApiGatewayException(JWT_TOKEN_EXPIRE);
        } catch (Exception e) {
            throw new ApiGatewayException(ACCESS_TOKEN_VALID_FAIL);
        }
    }

    public void tokenForgeryCheck(Long userId, String accessToken) {
        if (!userId.equals(getAudienceFromToken(accessToken))) {
            throw new ApiGatewayException(JWT_TOKEN_AND_USER_ID_NOT_VALID);
        }
    }

    public void tokenValid(String token, JwtTokenType jwtTokenType) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            if (jwtTokenType == ACCESS_TOKEN) {
                throw new ApiGatewayException(ACCESS_TOKEN_VALID_FAIL);
            } else {
                throw new ApiGatewayException(REFRESH_TOKEN_VALID_FAIL);
            }
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new ApiGatewayException(JWT_TOKEN_EXPIRE);
        }
    }
}
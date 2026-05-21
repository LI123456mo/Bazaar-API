package com.conel.market.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    public static final String TOKEN_TYPE = "token_type";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${app.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public JwtService() throws Exception {
        this.privateKey = KeyUtils.loadPrivateKey("keys/local-only/private_key.pem");
        this.publicKey = KeyUtils.loadPublicKey("keys/local-only/public_key.pem");
    }

    public String generateAccessToken(final String username){
        final Map<String,Object> claims=Map.of(TOKEN_TYPE,"ACCESS_TOKEN");
        return buildToken(username,claims,this.accessTokenExpiration);
    }

    public String generateRefreshToken(final String username){
        final Map<String,Object> claims=Map.of(TOKEN_TYPE,"REFRESH_TOKEN");
        return buildToken(username,claims,this.refreshTokenExpiration);
    }

    public String buildToken(final String username, final Map<String, Object> claims, final long expiration){
        return Jwts.builder()//HEADER
                //PAYLOAD
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                //SIGNATURE
                .signWith(this.privateKey)
                .compact();//header.payload.signature
    }
    
    public boolean isTokenValid(final String token,final String expectedUsername){
        final String username=extractUsername(token);
        return username.equals(expectedUsername) && !isTokenExpired(token);
    }

    private String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (final JwtException e){
            throw new RuntimeException("INVALID TOKEN",e);
        }
    }

    public String refreshAccessToken(final String refreshToken){
        final Claims claims=extractClaims(refreshToken);

        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE,String.class))){
            throw new RuntimeException("Invalid token type");
        }

        if (claims.getExpiration().before(new Date())){
            throw new RuntimeException("Refresh token expired");
        }

        final String username = claims.getSubject();
        return generateAccessToken(username);
    }
}

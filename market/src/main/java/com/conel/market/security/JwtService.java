package com.conel.market.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    public static final String TOKEN_TYPE = "token_type";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Value("${app.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.security.jwt.private-key-path:keys/local-only/private_key.pem}")
    private String privateKeyPath;

    @Value("${app.security.jwt.public-key-path:keys/local-only/public_key.pem}")
    private String publicKeyPath;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = KeyUtils.loadPrivateKey(privateKeyPath);
        this.publicKey = KeyUtils.loadPublicKey(publicKeyPath);
    }

    public String generateAccessToken(final String username){
        final Map<String,Object> claims = Map.of(TOKEN_TYPE, "ACCESS_TOKEN");
        return buildToken(username, claims, this.accessTokenExpiration);
    }

    public String generateRefreshToken(final String username){
        final Map<String,Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        return buildToken(username, claims, this.refreshTokenExpiration);
    }

    public String buildToken(final String username, final Map<String, Object> claims, final long expiration){
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(this.privateKey)
                .compact();
    }

    public boolean isTokenValid(final String token, final UserDetails userDetails) {
        try {
            final Claims claims = extractClaims(token);
            final String username = claims.getSubject();
            return username.equals(userDetails.getUsername()) && !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (JwtException e) {
            throw new RuntimeException("Failed to extract username from token", e);
        }
    }

    private Claims extractClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String refreshAccessToken(final String refreshToken){
        try {
            final Claims claims = extractClaims(refreshToken);

            if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE, String.class))){
                throw new RuntimeException("Invalid token type");
            }

            if (claims.getExpiration().before(new Date())){
                throw new RuntimeException("Refresh token expired");
            }

            return generateAccessToken(claims.getSubject());
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or malformed refresh token", e);
        }
    }
}

package com.conel.market.models.cart;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class GuestTokenService {
    private static final String COOKIE_NAME = "guest_cart_token";
    private static final int TOKEN_BYTES = 32;
    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 30;

    private final SecureRandom secureRandom = new SecureRandom();

    public String readToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String issueNewToken(HttpServletResponse response) {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO: set true once running behind real HTTPS in production
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);

        return token;
    }

    public void clearToken(HttpServletResponse response) {
        Cookie expired = new Cookie(COOKIE_NAME, null);
        expired.setPath("/");
        expired.setMaxAge(0);
        response.addCookie(expired);
    }
}
package com.conel.market.models.cart;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class GuestTokenService {
    private static final String COOKIE_NAME="guest_cart_token";
    private static final int TOKEN_BYTES=32;
    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 30;

    private final SecureRandom secureRandom = new SecureRandom();

    public String readToken(HttpServletRequest request){
        if (request.getCookies()==null)return null;
        for(Cookie cookie: request.getCookies()){
            if (COOKIE_NAME.equals(cookie.getName())){
                return cookie.getValue();
            }
        }
        return null;
    }
}

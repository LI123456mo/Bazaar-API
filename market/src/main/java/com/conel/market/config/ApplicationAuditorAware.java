package com.conel.market.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();

        if (authentication==null || !authentication.isAuthenticated() ||
        authentication instanceof AnonymousAuthenticationToken){
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }
}

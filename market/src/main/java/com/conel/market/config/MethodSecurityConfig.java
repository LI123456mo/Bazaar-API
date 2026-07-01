package com.conel.market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity // turns on @PreAuthorize support
public class MethodSecurityConfig {
}
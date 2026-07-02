package com.conel.market.models.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    // TODO: inject JavaMailSender when you add spring-boot-starter-mail
    // For now this logs the email instead of sending — safe for development

    public void sendVerificationEmail(String toEmail, String firstName, String verificationUrl) {
        // TODO: replace with real email sending
        log.info("=== VERIFICATION EMAIL ===");
        log.info("To: {}", toEmail);
        log.info("Hi {}, please verify your email:", firstName);
        log.info("URL: {}", verificationUrl);
        log.info("==========================");
    }
}
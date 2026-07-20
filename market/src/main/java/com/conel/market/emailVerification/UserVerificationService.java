package com.conel.market.emailVerification;

import com.conel.market.user.entity.User;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.entity.verification.VerificationToken;
import com.conel.market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserVerificationService {

    @Value("${app.email.verification-token-expiry:24}")
    private int tokenExpiryHours;

    @Value("${app.email.verification-base-url}")
    private String verificationBaseUrl;

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;


    @Transactional
    public void sendVerificationEmail(User user) {
        // Delete any existing unused tokens for this user first
        verificationTokenRepository.deleteByUserId(user.getId());

        // Create new token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(tokenExpiryHours))
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);

        // The email must point to the real controller route; a stale placeholder URL breaks verification in production.
        String verificationUrl = verificationBaseUrl + "/api/v1/auth/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationUrl);

        log.info("Verification email sent to {}", user.getEmail());
    }


    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN));

        if (verificationToken.isUsed()) {
            throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_ALREADY_USED);
        }

        if (LocalDateTime.now().isAfter(verificationToken.getExpiryDate())) {
            throw new BusinessException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        log.info("Email verified for user: {}", user.getEmail());
    }
}

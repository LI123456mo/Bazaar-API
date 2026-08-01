package com.conel.market.emailVerification;

import com.conel.market.auth.dto.request.PasswordResetRequest;
import com.conel.market.entity.verification.PasswordResetToken;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.user.entity.User;
import com.conel.market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int RESET_TOKEN_EXPIRY_HOURS = 1;

    @Value("${app.email.verification-base-url}")
    private String applicationBaseUrl;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void forgotPassword(String email) {
        // Never reveal whether the email exists so attackers cannot use this endpoint for account enumeration.
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            log.info("Password reset request processed for {}", email);
            return;
        }

        // Remove stale reset tokens before issuing a new one so a single user cannot keep multiple valid recovery links alive.
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String resetUrl = applicationBaseUrl + "/api/v1/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetUrl);

        log.info("Password reset email sent to {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        if (resetToken.isUsed()) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_USED);
        }

        if (LocalDateTime.now().isAfter(resetToken.getExpiryDate())) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marking the token as used preserves audit history and proves the reset link was consumed exactly once.
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset completed for {}", user.getEmail());
    }
}

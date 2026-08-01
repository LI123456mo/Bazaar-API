package com.conel.market.emailVerification;

import com.conel.market.entity.verification.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for password recovery tokens.
 * Each method supports a distinct recovery step so token lookup, invalidation, and existence checks stay explicit.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    /** Finds the token record required to validate and consume a reset link. */
    Optional<PasswordResetToken> findByToken(String token);

    /** Removes all reset tokens for a user so only one active recovery flow can exist at a time. */
    void deleteByUserId(String userId);

    /** Checks whether the user already has any reset token stored. */
    boolean existsByUserId(String userId);
}

package com.conel.market.repository.verification;

import com.conel.market.entity.verification.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {

    Optional<VerificationToken> findByToken(String token);

    boolean existsByUserId(String userId);

    void deleteByUserId(String userId);
}
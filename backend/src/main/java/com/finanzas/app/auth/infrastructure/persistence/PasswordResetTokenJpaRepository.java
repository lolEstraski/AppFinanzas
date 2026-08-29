package com.finanzas.app.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finanzas.app.auth.domain.PasswordResetToken;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("select p from PasswordResetToken p join fetch p.user where p.tokenHash = :tokenHash")
    Optional<PasswordResetToken> findByTokenHash(@Param("tokenHash") String tokenHash);
}

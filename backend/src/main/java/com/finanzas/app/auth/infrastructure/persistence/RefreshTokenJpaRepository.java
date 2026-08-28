package com.finanzas.app.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finanzas.app.auth.domain.RefreshToken;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {

    @Query("select r from RefreshToken r join fetch r.user where r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("update RefreshToken r set r.revoked = true where r.user.id = :userId")
    void revokeAllForUser(@Param("userId") Long userId);
}

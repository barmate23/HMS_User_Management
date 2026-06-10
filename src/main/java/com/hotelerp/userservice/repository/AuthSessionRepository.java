package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findByAccessTokenId(String accessTokenId);
    Optional<AuthSession> findByRefreshTokenId(String refreshTokenId);
    List<AuthSession> findByUserIdAndRevokedAtIsNull(Long userId);
}

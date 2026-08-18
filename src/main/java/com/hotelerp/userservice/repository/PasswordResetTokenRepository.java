package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByEmailAndResetCodeOrderByCreatedOnDesc(String email, String resetCode);
}

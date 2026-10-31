package com.dawn.auth.repository;

import com.dawn.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserCredential> findByUserId(Long userId);

    @Query("SELECT coalesce(max(u.userId), 1000) FROM UserCredential u")
    Long findMaxUserId();
}

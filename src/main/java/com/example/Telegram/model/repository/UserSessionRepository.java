package com.example.Telegram.model.repository;

import com.example.Telegram.model.data.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    UserSession findBySessionToken(String sessionToken);

    // Find UserSession by id
    Optional<UserSession> findById(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession u SET u.status = 'offline' WHERE u.id = :id")
    void updateStatusToOfflineById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession u SET u.status = 'offline' WHERE u.status != 'offline'")
    void updateAllStatusToOffline();
}

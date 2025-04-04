package com.thanhpro0703.SamNgocLinhPJ.reponsitory;

import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Integer> {
    
    Optional<UserSessionEntity> findByTokenId(String tokenId);
    
    List<UserSessionEntity> findByUser(UserEntity user);
    
    @Query("SELECT s FROM UserSessionEntity s WHERE s.user.id = :userId ORDER BY s.lastActivity DESC")
    List<UserSessionEntity> findRecentSessionsByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(LocalDateTime now);
    
    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.lastActivity = :now WHERE s.tokenId = :tokenId")
    void updateLastActivity(String tokenId, LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.user.id = :userId AND s.tokenId != :currentTokenId")
    void deleteOtherSessionsForUser(Long userId, String currentTokenId);
} 
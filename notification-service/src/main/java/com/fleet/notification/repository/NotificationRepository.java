package com.fleet.notification.repository;

import com.fleet.notification.entity.Notification;
import com.fleet.notification.entity.NotificationStatus;
import com.fleet.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);

    List<Notification> findByTypeAndRecipientId(NotificationType type, UUID recipientId);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.createdAt < :before")
    List<Notification> findPendingOlderThan(@Param("before") Instant before);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'FAILED', n.errorMessage = :error WHERE n.id = :id")
    void markAsFailed(@Param("id") UUID id, @Param("error") String error);

    long countByRecipientIdAndStatus(UUID recipientId, NotificationStatus status);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.type = :type")
    long countByStatusAndType(@Param("status") NotificationStatus status, @Param("type") NotificationType type);
}

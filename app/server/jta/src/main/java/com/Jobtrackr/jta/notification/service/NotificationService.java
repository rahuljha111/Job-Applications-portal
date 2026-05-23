package com.Jobtrackr.jta.notification.service;

import com.Jobtrackr.jta.exception.NotFoundException;
import com.Jobtrackr.jta.notification.dto.NotificationResponse;
import com.Jobtrackr.jta.notification.entity.Notification;
import com.Jobtrackr.jta.notification.entity.NotificationType;
import com.Jobtrackr.jta.notification.repository.NotificationRepository;
import com.Jobtrackr.jta.user.entity.User;
import com.Jobtrackr.jta.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Page<NotificationResponse> getNotifications(Pageable pageable) {
        User user = getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    public List<NotificationResponse> getUnreadNotifications() {
        User user = getCurrentUser();
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long getUnreadCount() {
        User user = getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        User user = getCurrentUser();
        int updated = notificationRepository.markAsRead(notificationId, user.getId());
        if (updated == 0) {
            throw new NotFoundException("Notification not found");
        }
        log.info("Notification marked as read: {} for user: {}", notificationId, user.getEmail());
    }

    @Transactional
    public int markAllAsRead() {
        User user = getCurrentUser();
        int count = notificationRepository.markAllAsRead(user.getId());
        log.info("Marked {} notifications as read for user: {}", count, user.getEmail());
        return count;
    }

    @Transactional
    public void createNotification(User user, NotificationType type, String title, String message,
                                   UUID relatedEntityId, String relatedEntityType) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);

        notificationRepository.save(notification);
        log.info("Notification created for user: {} - type: {}", user.getEmail(), type);
    }

    public void notifyApplicationStatusChange(User candidate, String jobTitle, String newStatus, UUID applicationId) {
        String title = "Application Status Updated";
        String message = String.format("Your application for '%s' has been %s.", jobTitle, newStatus.toLowerCase());
        
        NotificationType type = switch (newStatus) {
            case "SHORTLISTED" -> NotificationType.APPLICATION_SHORTLISTED;
            case "REJECTED" -> NotificationType.APPLICATION_REJECTED;
            case "HIRED" -> NotificationType.APPLICATION_HIRED;
            default -> NotificationType.APPLICATION_STATUS_CHANGED;
        };
        
        createNotification(candidate, type, title, message, applicationId, "APPLICATION");
    }

    public void notifyNewApplication(User recruiter, String candidateName, String jobTitle, UUID applicationId) {
        String title = "New Application Received";
        String message = String.format("%s applied for '%s'.", candidateName, jobTitle);
        createNotification(recruiter, NotificationType.APPLICATION_RECEIVED, title, message, applicationId, "APPLICATION");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new NotFoundException("Not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedEntityId(),
                notification.getRelatedEntityType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}

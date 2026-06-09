package com.pfe.itsm.notifications.service;

import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.notifications.domain.Notification;
import com.pfe.itsm.notifications.domain.NotificationType;
import com.pfe.itsm.notifications.dto.NotificationResponse;
import com.pfe.itsm.notifications.repository.NotificationRepository;
import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import com.pfe.itsm.users.repository.UserAccountRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserAccountRepository userAccountRepository;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserAccountRepository userAccountRepository,
            CurrentUserService currentUserService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.userAccountRepository = userAccountRepository;
        this.currentUserService = currentUserService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public NotificationResponse notifyUser(
            UserAccount recipient,
            NotificationType type,
            String title,
            String message,
            String resourceType,
            UUID resourceId
    ) {
        Notification notification = notificationRepository.save(new Notification(
                recipient,
                type,
                title,
                message,
                resourceType,
                resourceId
        ));
        NotificationResponse response = NotificationResponse.from(notification);
        messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", response);
        return response;
    }

    @Transactional
    public void notifyRole(
            UserRole role,
            NotificationType type,
            String title,
            String message,
            String resourceType,
            UUID resourceId
    ) {
        userAccountRepository.findByRoleAndActifTrueAndEmailVerifiedTrue(role)
                .forEach(user -> notifyUser(user, type, title, message, resourceType, resourceId));
    }

    public void publishTicketUpdate(UUID ticketId, Object payload) {
        messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, payload);
    }

    public void publishQueueUpdate(SupportLevel level, Object payload) {
        messagingTemplate.convertAndSend("/topic/queues/" + level.name().toLowerCase(), payload);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listCurrentUserNotifications() {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserService.currentUserId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(UUID notificationId) {
        UserAccount user = currentUserService.currentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable."));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new BusinessException("Acces a la notification non autorise.");
        }
        notification.markRead();
        return NotificationResponse.from(notification);
    }
}


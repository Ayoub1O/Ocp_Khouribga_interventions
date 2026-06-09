package com.pfe.itsm.notifications.dto;

import com.pfe.itsm.notifications.domain.Notification;
import com.pfe.itsm.notifications.domain.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        String resourceType,
        UUID resourceId,
        Instant createdAt,
        Instant readAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getResourceType(),
                notification.getResourceId(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}



package com.emenu.features.notification.factory;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationPriority;
import com.emenu.enums.notification.NotificationRecipientType;
import com.emenu.features.notification.models.Notification;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationFactory {

    /**
     * Create individual user notification
     */
    public Notification createUserNotification(
            String title,
            String message,
            MessageType messageType,
            UUID userId,
            String userName,
            NotificationPriority priority) {

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMessageType(messageType);
        notification.setUserId(userId);
        notification.setUserName(userName);
        notification.setRecipientType(NotificationRecipientType.INDIVIDUAL_USER);
        notification.setPriority(priority != null ? priority : NotificationPriority.NORMAL);
        notification.setStatus(MessageStatus.SENT);
        notification.setIsRead(false);
        return notification;
    }

    /**
     * Create business team notification (for one member)
     */
    public Notification createBusinessTeamNotification(
            String title,
            String message,
            MessageType messageType,
            UUID businessId,
            UUID userId,
            String userName,
            NotificationPriority priority,
            UUID groupId) {

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMessageType(messageType);
        notification.setBusinessId(businessId);
        notification.setUserId(userId);
        notification.setUserName(userName);
        notification.setRecipientType(NotificationRecipientType.BUSINESS_TEAM_GROUP);
        notification.setPriority(priority != null ? priority : NotificationPriority.NORMAL);
        notification.setGroupId(groupId);
        notification.setStatus(MessageStatus.SENT);
        notification.setIsRead(false);
        return notification;
    }

    /**
     * Create system owner notification (for one owner)
     */
    public Notification createSystemOwnerNotification(
            String title,
            String message,
            MessageType messageType,
            UUID userId,
            String userName,
            NotificationPriority priority,
            UUID groupId) {

        Notification notification = new Notification();
        notification.setTitle("[SYSTEM] " + title);
        notification.setMessage(message);
        notification.setMessageType(messageType);
        notification.setUserId(userId);
        notification.setUserName(userName);
        notification.setRecipientType(NotificationRecipientType.SYSTEM_OWNER_GROUP);
        notification.setPriority(priority != null ? priority : NotificationPriority.HIGH);
        notification.setGroupId(groupId);
        notification.setStatus(MessageStatus.SENT);
        notification.setIsRead(false);
        return notification;
    }

    /**
     * Create all users notification (for one user)
     */
    public Notification createAllUsersNotification(
            String title,
            String message,
            MessageType messageType,
            UUID userId,
            String userName,
            NotificationPriority priority,
            UUID groupId) {

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMessageType(messageType);
        notification.setUserId(userId);
        notification.setUserName(userName);
        notification.setRecipientType(NotificationRecipientType.ALL_USERS);
        notification.setPriority(priority != null ? priority : NotificationPriority.NORMAL);
        notification.setGroupId(groupId);
        notification.setStatus(MessageStatus.SENT);
        notification.setIsRead(false);
        return notification;
    }

    /**
     * Create system copy of notification for monitoring
     */
    public Notification createSystemCopy(
            Notification original,
            UUID systemOwnerId,
            String systemOwnerName,
            UUID groupId) {

        Notification systemCopy = new Notification();
        systemCopy.setTitle("[Alert] " + original.getTitle());
        
        String enhancedMessage = String.format(
            "📢 User: %s\n🏢 Business: %s\n\n%s",
            original.getUserName() != null ? original.getUserName() : "N/A",
            original.getBusinessId() != null ? original.getBusinessId().toString() : "N/A",
            original.getMessage()
        );
        systemCopy.setMessage(enhancedMessage);
        
        systemCopy.setMessageType(original.getMessageType());
        systemCopy.setUserId(systemOwnerId);
        systemCopy.setUserName(systemOwnerName);
        systemCopy.setBusinessId(original.getBusinessId());
        systemCopy.setRecipientType(NotificationRecipientType.SYSTEM_OWNER_GROUP);
        systemCopy.setPriority(NotificationPriority.HIGH);
        systemCopy.setGroupId(groupId);
        systemCopy.setStatus(MessageStatus.SENT);
        systemCopy.setIsRead(false);
        
        return systemCopy;
    }
}
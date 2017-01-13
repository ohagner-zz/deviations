package com.ohagner.deviations.api.notification.service

import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.user.domain.User
import groovy.util.logging.Slf4j

@Slf4j
class DefaultNotificationService implements NotificationService {

    Set<Notifier> notifiers

    DefaultNotificationService(Set<Notifier> notifiers) {
        this.notifiers = notifiers
    }

    void sendNotification(User user, Notification notification) {
        notifiers.each { Notifier notifier ->
            if (notifier.isApplicable(notification.notificationTypes ?: [])) {
                try {
                    notifier.notify(user, notification)
                } catch(Exception e) {
                    log.error("NOTIFICATION FAILED for user ${user?.credentials?.username}. Notification: ${notifier.class.name}", e)
                }
            }
        }
    }
}

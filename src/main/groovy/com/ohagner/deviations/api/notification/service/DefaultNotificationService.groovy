package com.ohagner.deviations.api.notification.service

import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.user.domain.User

class DefaultNotificationService implements NotificationService {

    Set<Notifier> notifiers

    DefaultNotificationService(Set<Notifier> notifiers) {
        this.notifiers = notifiers
    }

    void sendNotification(User user, Notification notification) {
        notifiers.each { Notifier notifier ->
            if (notifier.isApplicable(notification.notificationTypes ?: [])) {
                notifier.notify(user, notification)
            }
        }
    }
}

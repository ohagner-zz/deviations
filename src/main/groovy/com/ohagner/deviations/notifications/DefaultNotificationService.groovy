package com.ohagner.deviations.notifications

import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.domain.notification.Notification

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

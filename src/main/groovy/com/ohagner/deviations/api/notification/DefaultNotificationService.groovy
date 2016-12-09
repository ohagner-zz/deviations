package com.ohagner.deviations.api.notification

import com.ohagner.deviations.api.user.User

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

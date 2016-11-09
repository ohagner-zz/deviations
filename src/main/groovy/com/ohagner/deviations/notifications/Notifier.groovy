package com.ohagner.deviations.notifications

import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.domain.notification.Notification
import com.ohagner.deviations.domain.notification.NotificationType

interface Notifier {

    boolean isApplicable(Collection<NotificationType> notificationTypes)

    void notify(User user, Notification notification)

}
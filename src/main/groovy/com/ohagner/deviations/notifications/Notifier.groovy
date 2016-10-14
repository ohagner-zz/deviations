package com.ohagner.deviations.notifications

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.notifications.Notification
import com.ohagner.deviations.domain.notifications.NotificationType

interface Notifier {

    boolean isApplicable(Collection<NotificationType> notificationTypes)

    void notify(User user, Notification notification)

}
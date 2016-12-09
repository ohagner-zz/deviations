package com.ohagner.deviations.api.notification

import com.ohagner.deviations.api.user.User
import com.ohagner.deviations.api.notification.Notification
import com.ohagner.deviations.api.notification.NotificationType

interface Notifier {

    boolean isApplicable(Collection<NotificationType> notificationTypes)

    void notify(User user, Notification notification)

}
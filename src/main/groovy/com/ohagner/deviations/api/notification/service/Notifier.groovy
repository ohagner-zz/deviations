package com.ohagner.deviations.api.notification.service

import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.notification.domain.NotificationType
import com.ohagner.deviations.api.user.domain.User

interface Notifier {

    boolean isApplicable(Collection<NotificationType> notificationTypes)

    void notify(User user, Notification notification)

}
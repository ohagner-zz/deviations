package com.ohagner.deviations.api.notification.service

import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.user.domain.User

interface NotificationService {

    void sendNotification(User user, Notification notification)
}

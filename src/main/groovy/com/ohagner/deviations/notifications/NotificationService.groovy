package com.ohagner.deviations.notifications

import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.domain.notification.Notification

interface NotificationService {

    void sendNotification(User user, Notification notification)
}

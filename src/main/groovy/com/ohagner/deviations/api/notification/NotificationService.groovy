package com.ohagner.deviations.api.notification

import com.ohagner.deviations.api.user.User
import com.ohagner.deviations.api.notification.Notification

interface NotificationService {

    void sendNotification(User user, Notification notification)
}

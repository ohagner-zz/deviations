package com.ohagner.deviations.notifications

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.notifications.Notification

interface NotificationService {

    void sendNotification(User user, Notification notification)
}

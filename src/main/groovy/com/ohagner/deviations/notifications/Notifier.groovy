package com.ohagner.deviations.notifications

import com.ohagner.deviations.Deviation
import com.ohagner.deviations.User
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.notifications.NotificationType

interface Notifier {

    boolean isApplicable(List<NotificationType> notificationTypes)

    def notify(User user, Set<Deviation> deviations)

}
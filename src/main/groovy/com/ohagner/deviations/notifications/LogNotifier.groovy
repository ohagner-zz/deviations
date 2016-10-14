package com.ohagner.deviations.notifications

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.notifications.Notification
import com.ohagner.deviations.domain.notifications.NotificationType
import groovy.util.logging.Slf4j

@Slf4j
class LogNotifier implements Notifier {


    boolean isApplicable(Collection<NotificationType> notificationTypes) {
        return notificationTypes.contains(NotificationType.LOG)
    }

    @Override
    void notify(User user, Notification notification) {
        def logMessage = ""
        logMessage += "User ${user.username} was notified that: ${notification.header}\n"
        logMessage += notification.message
        log.info(logMessage)
    }
}

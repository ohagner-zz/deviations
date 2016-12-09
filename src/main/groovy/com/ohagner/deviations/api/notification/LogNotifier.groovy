package com.ohagner.deviations.api.notification

import com.ohagner.deviations.api.user.User
import groovy.util.logging.Slf4j

@Slf4j
class LogNotifier implements Notifier {


    boolean isApplicable(Collection<NotificationType> notificationTypes) {
        return notificationTypes.contains(NotificationType.LOG)
    }

    @Override
    void notify(User user, Notification notification) {
        def logMessage = ""
        logMessage += "User ${user.credentials.username} was notified that: ${notification.header}\n"
        logMessage += notification.message
        log.info(logMessage)
    }
}

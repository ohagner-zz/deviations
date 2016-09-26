package com.ohagner.deviations.notifications

import com.ohagner.deviations.Deviation
import com.ohagner.deviations.User
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.notifications.NotificationType
import groovy.util.logging.Slf4j

@Slf4j
class LogNotifier implements Notifier {


    boolean isApplicable(List<NotificationType> notificationTypes) {
        return notificationTypes.contains(NotificationType.LOG)
    }

    @Override
    def notify(User user, Set<Deviation> deviations) {
        def logMessage = ""
        logMessage += "User ${user.username} has ${deviations.size()} notification${deviations.size() > 1 ? 's' : ''}\n"
        logMessage += "LineNumbers".padRight(20) + "Details\n"
        deviations.each {
            logMessage += it.lineNumbers.join(",").padRight(20) + it.details
        }
        log.info(logMessage)
    }
}
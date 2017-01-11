package com.ohagner.deviations.api.notification.service

import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.notification.domain.NotificationType
import com.ohagner.deviations.api.user.domain.User
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import wslite.rest.RESTClient
import wslite.rest.Response

@Slf4j
@TupleConstructor
class EmailNotifier implements Notifier {

    static supportedNotificationTypes = [NotificationType.EMAIL]

    private RESTClient restClient
    private String sender

    boolean isApplicable(Collection<NotificationType> notificationTypes) {
        return !supportedNotificationTypes.intersect(notificationTypes).isEmpty()
    }

    @Override
    void notify(User user, Notification notification) {

        log.info "Sending email to ${user.emailAddress} about ${notification.header}"

        log.debug "Email text: ${notification.message}"

        Response response = restClient.post() {
            multipart 'to', "${user.emailAddress}".bytes
            multipart 'from', sender.bytes
            multipart 'subject', notification.header.bytes
            multipart 'text', notification.message.bytes
        }

        log.debug "Send email response: ${response.toString()}"
    }


}

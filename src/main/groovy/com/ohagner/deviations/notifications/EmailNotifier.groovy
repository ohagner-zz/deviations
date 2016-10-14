package com.ohagner.deviations.notifications

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.notifications.Notification
import com.ohagner.deviations.domain.notifications.NotificationType
import groovy.util.logging.Slf4j
import wslite.rest.RESTClient
import wslite.rest.Response

@Slf4j
class EmailNotifier implements Notifier {

    static supportedNotificationTypes = [NotificationType.EMAIL]

    private RESTClient restClient

    EmailNotifier(RESTClient restClient) {
        this.restClient = restClient
    }

    boolean isApplicable(Collection<NotificationType> notificationTypes) {
        return !supportedNotificationTypes.intersect(notificationTypes).isEmpty()
    }

    @Override
    void notify(User user, Notification notification) {
        //RESTClient client = new RESTClient('https://api.mailgun.net/v3/sandbox8e1378b0675e4dfeaf914d9d6b710afa.mailgun.org/messages')
        //client.authorization = new HTTPBasicAuthorization('api', 'key-221c9a9de2862b986a05a43ac92070cf')

        log.info "Sending email to ${user.emailAddress} about ${notification.header}"

        log.info "Text: ${notification.message}"
        Response response = restClient.post() {
            multipart 'to', "${user.emailAddress}".bytes
            multipart 'from', 'Deviation <postmaster@sandbox8e1378b0675e4dfeaf914d9d6b710afa.mailgun.org>'.bytes
            multipart 'subject', notification.header.bytes
            multipart 'text', notification.message.bytes
        }

        log.info response.toString()
    }


}

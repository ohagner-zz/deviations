package com.ohagner.deviations.notifications

import com.ohagner.deviations.Deviation
import com.ohagner.deviations.User
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.notifications.NotificationType
import groovy.util.logging.Slf4j
import wslite.http.auth.HTTPBasicAuthorization
import wslite.rest.RESTClient
import wslite.rest.Response

@Slf4j
class EmailNotifier implements Notifier {

    static supportedNotificationTypes = [NotificationType.EMAIL]

    boolean isApplicable(List<NotificationType> notificationTypes) {
        return !supportedNotificationTypes.intersect(notificationTypes).isEmpty()
    }

    @Override
    def notify(User user, Set<Deviation> deviations) {
        RESTClient client = new RESTClient('https://api.mailgun.net/v3/sandbox8e1378b0675e4dfeaf914d9d6b710afa.mailgun.org/messages')
        client.authorization = new HTTPBasicAuthorization('api', 'key-221c9a9de2862b986a05a43ac92070cf')

        log.info "Sending email to ${user.emailAddress} about ${deviations.size()} notifications"

        String text = deviations.collect {toEmailBody(it)}.join("\n")
        log.info "Text: $text"

        Response response = client.post() {
            multipart 'to', "${user.emailAddress}".bytes
            multipart 'from', 'Deviation <postmaster@sandbox8e1378b0675e4dfeaf914d9d6b710afa.mailgun.org>'.bytes
            multipart 'subject', 'Deviation notification'.bytes
            multipart 'text', text.bytes
        }

        log.info response.toString()

    }

    String toEmailBody(Deviation deviation) {
        """
            Linjer: ${deviation.lineNumbers.join(",")}
            Rubrik: ${deviation.header}
            Gäller från: ${deviation.from}
            Gäller till: ${deviation.to}
            Uppdaterad: ${deviation.updated}
        """
    }
}

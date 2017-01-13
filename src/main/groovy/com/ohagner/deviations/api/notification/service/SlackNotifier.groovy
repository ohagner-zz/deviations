package com.ohagner.deviations.api.notification.service

import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.notification.domain.NotificationType
import com.ohagner.deviations.api.user.domain.User
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.Response

@Slf4j
class SlackNotifier implements Notifier {

    static supportedNotificationTypes = [NotificationType.SLACK]

    @Override
    boolean isApplicable(Collection<NotificationType> notificationTypes) {
        return !supportedNotificationTypes.intersect(notificationTypes).isEmpty()
    }

    @Override
    void notify(User user, Notification notification) {
        log.info "Sending slack notification to ${user.slackWebhook.url}"
        RESTClient restClient = new RESTClient(user.slackWebhook.url.toString())
        String request = JsonOutput.toJson {
            username "Trafikbevakaren"
            icon_emoji ":station:"
            text notification.message
        }

        Response response = restClient.post() {
            type ContentType.JSON
            text request
        }
        log.info "Slack notifier response: ${response.statusCode} ${response.contentAsString}"
    }
}

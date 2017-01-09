package com.ohagner.deviations.api.notification.service

import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.notification.domain.NotificationType
import com.ohagner.deviations.api.user.domain.User
import groovy.util.logging.Slf4j
import wslite.rest.ContentType
import wslite.rest.RESTClient

@Slf4j
class WebhookNotifier implements Notifier {

    static supportedNotificationTypes = [NotificationType.WEBHOOK]

    @Override
    boolean isApplicable(Collection<NotificationType> notificationTypes) {
        return !supportedNotificationTypes.intersect(notificationTypes).isEmpty()
    }

    @Override
    void notify(User user, Notification notification) {
        log.info "Sending webhook notification to ${user.webhook.url}"
        RESTClient restClient = new RESTClient(user.webhook.url.toString())
        restClient.post() {
            type ContentType.JSON
            text notification.toJson()
        }
    }
}

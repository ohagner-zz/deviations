package com.ohagner.deviations.api.notification.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.multibindings.Multibinder
import com.ohagner.deviations.api.notification.service.DefaultNotificationService
import com.ohagner.deviations.api.notification.service.EmailNotifier
import com.ohagner.deviations.api.notification.service.LogNotifier
import com.ohagner.deviations.api.notification.service.NotificationService
import com.ohagner.deviations.api.notification.service.Notifier
import com.ohagner.deviations.api.notification.service.SlackNotifier
import com.ohagner.deviations.api.notification.service.WebhookNotifier
import wslite.http.auth.HTTPBasicAuthorization
import wslite.rest.RESTClient

import static com.ohagner.deviations.config.AppConfig.envOrProperty

class NotificationsModule extends AbstractModule {


    @Override
    protected void configure() {
        Multibinder<Notifier> binder = Multibinder.newSetBinder(binder(), Notifier.class)
        binder.addBinding().to(EmailNotifier)
        binder.addBinding().to(LogNotifier)
        binder.addBinding().to(WebhookNotifier)
    }

    static class Config {

        public static final String EMAIL_SERVICE_URL = "EMAIL_SERVICE_URL"
        public static final String EMAIL_SERVICE_API_KEY = "EMAIL_SERVICE_API_KEY"

        String emailServiceUrl
        String emailServiceApiKey

        static NotificationsModule.Config getInstance() {
            NotificationsModule.Config instance = new NotificationsModule.Config()
            instance.with {
                emailServiceUrl = envOrProperty(EMAIL_SERVICE_URL)
                emailServiceApiKey = envOrProperty(EMAIL_SERVICE_API_KEY)
            }
            return instance
        }
    }

    @Provides
    NotificationService createNotificationsService(Set<Notifier> notifiers) {
        return new DefaultNotificationService(notifiers)
    }

    @Provides
    EmailNotifier createEmailNotifier() {
        NotificationsModule.Config config = NotificationsModule.Config.getInstance()

        RESTClient client = new RESTClient(config.emailServiceUrl)
        client.authorization = new HTTPBasicAuthorization('api', config.emailServiceApiKey)
        return new EmailNotifier(client)
    }

    @Provides
    LogNotifier createLogNotifier() {
        return new LogNotifier()
    }

    @Provides
    WebhookNotifier createWebhookNotifier() {
        return new WebhookNotifier()
    }

    @Provides
    SlackNotifier createSlackNotifier() {
        return new SlackNotifier()
    }


}

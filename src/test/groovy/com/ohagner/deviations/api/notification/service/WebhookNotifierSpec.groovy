package com.ohagner.deviations.api.notification.service

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.domain.Webhook
import org.junit.Rule
import spock.lang.Specification
import static com.github.tomakehurst.wiremock.client.WireMock.*

class WebhookNotifierSpec extends Specification {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089)

    WebhookNotifier webhookNotifier = new WebhookNotifier()

    def setup() {
        wireMockRule.stubFor(post(urlEqualTo("/webhook"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                .withStatus(200)))
    }

    void 'should make call to webhook'() {
        given:
            User user = new User(webhook: Webhook.newInstance("http://localhost:8089/webhook"))
            Notification notification = new Notification(header: "Header", message: "Message")
        when:
            webhookNotifier.notify(user, notification)
        then:
            verify(
                    postRequestedFor(urlMatching("/webhook"))
                            .withRequestBody(equalToJson(notification.toJson()))
            )
    }

}

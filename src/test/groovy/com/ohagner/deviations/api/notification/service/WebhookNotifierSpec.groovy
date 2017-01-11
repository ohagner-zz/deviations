package com.ohagner.deviations.api.notification.service

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.domain.Webhook
import org.junit.Rule
import spock.lang.Specification
import wslite.rest.RESTClientException

import static com.github.tomakehurst.wiremock.client.WireMock.*

class WebhookNotifierSpec extends Specification {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089)

    WebhookNotifier webhookNotifier = new WebhookNotifier()
    User user = new User(webhook: Webhook.newInstance("http://localhost:8089/webhook"))
    Notification notification = new Notification(header: "Header", message: "Message")

    void 'should make call to webhook'() {
        given:
            wireMockRule.stubFor(post(urlEqualTo("/webhook"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                    .withStatus(200)))
        when:
            webhookNotifier.notify(user, notification)
        then:
            verify(
                    postRequestedFor(urlMatching("/webhook"))
                            .withRequestBody(equalToJson(notification.toJson()))
            )
    }

    void 'should throw exception when webhook call fails'() {
        given:
            wireMockRule.stubFor(post(urlEqualTo("/webhook"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                    .withStatus(500)))
        when:
            webhookNotifier.notify(user, notification)
        then:
            thrown(RESTClientException)
    }

}

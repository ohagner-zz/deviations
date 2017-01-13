package com.ohagner.deviations.api.notification.service

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.user.domain.User
import org.junit.Rule
import spock.lang.Specification
import wslite.rest.RESTClient
import wslite.rest.RESTClientException

import static com.github.tomakehurst.wiremock.client.WireMock.*

class EmailNotifierSpec extends Specification {

    public static final int PORT = 8089
    public static final String PATH = "email"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089)

    EmailNotifier emailNotifier
    User user = new User(emailAddress: "test@test.com")
    Notification notification = new Notification(header: "Header", message: "Message")

    def setup() {
        emailNotifier = new EmailNotifier(restClient: new RESTClient("http://localhost:$PORT/$PATH"), sender: "Trafikbevakaren <no-reply@trafikbevakaren.se>")
    }

    void 'should make call to email service'() {
        given:
            wireMockRule.stubFor(post(urlEqualTo("/$PATH"))
                    .withHeader("Content-Type", containing("multipart"))
                    .willReturn(aResponse()
                    .withStatus(200)))
        when:
            emailNotifier.notify(user, notification)
        then:
            verify(
                    postRequestedFor(urlMatching("/$PATH"))
            )
    }

    void 'should throw exception when email service call fails'() {
        given:
            wireMockRule.stubFor(post(urlEqualTo("/$PATH"))
                    .withHeader("Content-Type", containing("multipart"))
                    .willReturn(aResponse()
                    .withStatus(500)))
        when:
            emailNotifier.notify(user, notification)
        then:
            thrown(RESTClientException)
    }
}

package com.ohagner.deviations.api.notification.domain

import com.ohagner.deviations.api.deviation.domain.Deviation
import groovy.json.JsonOutput
import spock.lang.Specification

import java.time.LocalDateTime
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals
import static org.hamcrest.MatcherAssert.assertThat

class NotificationSpec extends Specification {

    private static final String EXPECTED_JSON = JsonOutput.toJson {
        header "Header"
        message "Message"
        notificationTypes([NotificationType.EMAIL, NotificationType.LOG])
    }

    private static final Notification NOTIFICATION = new Notification(header: "Header", message: "Message", notificationTypes: [NotificationType.EMAIL, NotificationType.LOG])

    void 'should transform deviations to notification'() {
        given:
            def deviations = [createDeviation(), createDeviation()]
        expect:
            Notification.fromDeviations(deviations, [NotificationType.EMAIL, NotificationType.LOG])
    }

    void 'should create json from object'() {
        expect:
            assertThat(NOTIFICATION.toJson(), jsonEquals(EXPECTED_JSON))
    }

    void 'should create notification from json'() {
        expect:
            Notification.fromJson(EXPECTED_JSON).equals(NOTIFICATION)
    }

    private createDeviation() {
        LocalDateTime now = LocalDateTime.now()

        return Deviation.builder()
            .header("Header")
            .details("Details")
            .lineNumbers(["1", "2"])
            .created(now)
            .updated(now)
            .from(now)
            .to(now)
            .build()
    }

}

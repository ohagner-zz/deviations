package com.ohagner.deviations.api.notification.domain

import com.ohagner.deviations.api.deviation.domain.Deviation
import spock.lang.Specification

import java.time.LocalDateTime

class NotificationSpec extends Specification {

    void 'should transform deviations to notification'() {
        given:
            def deviations = [createDeviation(), createDeviation()]
        expect:
            Notification.fromDeviations(deviations, [NotificationType.EMAIL, NotificationType.LOG])
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

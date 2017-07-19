package com.ohagner.deviations

import com.ohagner.deviations.api.deviation.repository.DeviationFilter
import com.ohagner.deviations.api.deviation.domain.Deviation
import spock.lang.Specification

import java.time.LocalDateTime

import static com.ohagner.deviations.config.Constants.ZONE_ID
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

class DeviationFilterSpec extends Specification {

    void "filter out deviation with long duration"() {
        given:
            Deviation longDuration =longDuration()
        expect:
            assertThat(DeviationFilter.apply([longDuration]).size(), is(0))
    }

    void "filter out deviations from the past"() {
        given:
            Deviation oldDeviation = expired()
        expect:
            assertThat(DeviationFilter.apply([oldDeviation]).size(), is(0))
    }

    void "keep valid deviations"() {
        given:
            List<Deviation> deviations = [expired(), longDuration(), valid()]
        expect:
            assertThat(DeviationFilter.apply(deviations).size(), is(1))
    }

    private Deviation longDuration() {
        return Deviation.builder()
                .from(LocalDateTime.now(ZONE_ID).minusHours(13))
                .to(LocalDateTime.now(ZONE_ID).plusHours(1))
                .created(LocalDateTime.now(ZONE_ID))
                .build()
    }

    private Deviation expired() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID)
        return Deviation.builder()
                .to(now.minusHours(1))
                .from(now.minusHours(3))
                .build()
    }

    private Deviation valid() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID)
        return Deviation.builder()
                .to(now.plusHours(1))
                .from(now.minusHours(3))
                .build()
    }


}

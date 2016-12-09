package com.ohagner.deviations

import com.ohagner.deviations.api.deviation.DeviationFilter
import com.ohagner.deviations.api.deviation.Deviation
import spock.lang.Specification

import java.time.LocalDateTime

import static com.ohagner.deviations.config.Constants.ZONE_ID
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

class DeviationFilterSpec extends Specification {

    def "filter out deviation with long duration"() {
        given:
            Deviation longDuration = Deviation.builder()
                .from(LocalDateTime.now(ZONE_ID).minusHours(13))
                .to(LocalDateTime.now(ZONE_ID).plusHours(1))
                .created(LocalDateTime.now(ZONE_ID))
                .build()
        expect:
            assertThat(DeviationFilter.apply([longDuration]).size(), is(0))
    }

    def "filter out deviations from the past"() {
        given:
            LocalDateTime now = LocalDateTime.now(ZONE_ID)
            Deviation oldDeviation = Deviation.builder()
                    .to(now.minusHours(1))
                    .from(now.minusHours(3))
                    .build()
        expect:
            assertThat(DeviationFilter.apply([oldDeviation]).size(), is(0))
    }

}

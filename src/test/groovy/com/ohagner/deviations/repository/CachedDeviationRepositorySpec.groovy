package com.ohagner.deviations.repository

import com.ohagner.deviations.api.deviation.CachedDeviationRepository
import com.ohagner.deviations.api.deviation.Deviation
import com.ohagner.deviations.api.deviation.DeviationRepository
import spock.lang.Specification

import java.time.Duration

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat

class CachedDeviationRepositorySpec extends Specification {

    Deviation one = new Deviation(id: "1")
    Deviation two = new Deviation(id: "2")

    def "should update for both calls"() {
        given:
            DeviationRepository source = Mock()
            source.retrieveAll() >> [one] >> [two]
            CachedDeviationRepository cachedRepo = new CachedDeviationRepository(source, Duration.ofMillis(10L))
        when:
            Deviation firstResponse = cachedRepo.retrieveAll().first()
            Thread.sleep(1000)
            Deviation secondResponse = cachedRepo.retrieveAll().first()
        then:
            assertThat(firstResponse.id, is(one.id))
            assertThat(secondResponse.id, is(two.id))
    }

    def "should return same value for both calls"() {
        given:
            DeviationRepository source = Mock()
            source.retrieveAll() >> [one] >> [two]
            CachedDeviationRepository cachedRepo = new CachedDeviationRepository(source, Duration.ofMinutes(30))
        when:
            Deviation firstResponse = cachedRepo.retrieveAll().first()
            Deviation secondResponse = cachedRepo.retrieveAll().first()
        then:
            assertThat(firstResponse.id, is(one.id))
            assertThat(secondResponse.id, is(one.id))
    }


}

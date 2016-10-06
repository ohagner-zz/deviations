package com.ohagner.deviations

import com.ohagner.deviations.domain.Deviation
import com.ohagner.deviations.domain.Transport
import com.ohagner.deviations.domain.TransportMode
import com.ohagner.deviations.domain.Watch
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId

import static org.hamcrest.CoreMatchers.hasItems
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat
import static com.ohagner.deviations.config.Constants.ZONE_ID

class DeviationMatcherSpec extends Specification {

    static DeviationMatcher matcher

    def setupSpec() {
        matcher = new DeviationMatcher(createDeviationList())
    }

    def "match Train transports for line 1"() {
        given:
            Watch watch = new Watch(transports: [new Transport(transportMode: TransportMode.TRAIN, line: "1")])
        when:
            Set<Deviation> matching = matcher.findMatching(watch)
        then:
            assertThat(matching.size(), is(2))
            assertThat(matching.collect { it.header}, hasItems("First", "Second"))
    }

    def "match TRAIN transports for line 1 with id 1 already processed"() {
        given:
            Watch watch = new Watch(transports: [new Transport(transportMode: TransportMode.TRAIN, line: "1")])
            watch.processedDeviationIds.add("1")
        when:
            Set<Deviation> matching = matcher.findMatching(watch)
        then:
        assertThat(matching.size(), is(1))
        assertThat(matching.collect { it.header}, hasItems("Second"))
    }

    def "match BUS transport for line 3"() {
        given:
            Watch watch = new Watch(transports: [new Transport(transportMode: TransportMode.BUS, line: "3")])
        when:
            def matching = matcher.findMatching(watch)
        then:
            assertThat(matching.size(), is(1))
            assertThat(matching[0].header, is("Third"))
    }

    def "match none for TRAIN and line 4"() {
        given:
            Watch watch = new Watch(transports: [new Transport(transportMode: TransportMode.TRAIN, line: "4")])
        when:
            def matching = matcher.findMatching(watch)
        then:
            assertThat(matcher.transportDeviationMap.size(), is(5))
            assertThat(matching.size(), is(0))
    }

    def "filter out deviation with long duration"() {
        given:
            Deviation longDuration = Deviation.builder()
                    .from(LocalDateTime.now(ZONE_ID).minusHours(13))
                    .to(LocalDateTime.now(ZONE_ID).plusHours(1))
                    .created(LocalDateTime.now(ZONE_ID))
                    .build()
            DeviationMatcher deviationMatcher = new DeviationMatcher([longDuration])
        expect:
            assertThat(deviationMatcher.transportDeviationMap.size(), is(0))
    }

    def "filter out deviations created more than 12 hours ago"() {
        given:
            LocalDateTime now = LocalDateTime.now(ZONE_ID)
            Deviation tooLongDuration = Deviation.builder()
                    .from(now.minusHours(DeviationMatcher.MAX_DEVIATION_DURATION_HOURS))
                    .to(now.plusHours(1))
                    .build()
        expect:
            assertThat([tooLongDuration].findAll { DeviationMatcher.maxDurationPredicate(it) }.size(), is(0))
    }

    def "filter out deviations from the past"() {
        given:
            LocalDateTime now = LocalDateTime.now(ZONE_ID)
            Deviation oldDeviation = Deviation.builder()
                .to(now.minusHours(1))
                .build()
        expect:
            assertThat([oldDeviation].findAll { DeviationMatcher.stillActualPredicate(it) }.size(), is(0))
    }

    static List<Deviation> createDeviationList() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID)
        List<Deviation> deviations = []
        deviations << new Deviation(id: "1", lineNumbers: ["1", "2", "3"], transportMode: TransportMode.TRAIN, header: "First", from: now.minusHours(10), to: now.plusHours(1), created: now)
        deviations << new Deviation(id: "2", lineNumbers: ["1"], transportMode: TransportMode.TRAIN, header: "Second", from: now.minusHours(1), to: now.plusHours(1), created: now)
        deviations << new Deviation(id: "3", lineNumbers: ["3"], transportMode: TransportMode.BUS, header: "Third", from: now.minusHours(1), to: now.plusHours(1), created: now)
        return deviations
    }

    static Deviation createValidDeviation() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID)
        return new Deviation(lineNumbers: ["1"], transportMode: TransportMode.TRAIN, header: "Second", from: now, to: now, created: now)
    }

}

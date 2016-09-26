package com.ohagner.deviations

import com.ohagner.deviations.domain.Transport
import com.ohagner.deviations.domain.TransportMode
import com.ohagner.deviations.domain.Watch
import spock.lang.Specification

import static org.hamcrest.CoreMatchers.hasItems
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

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
            assertThat(matching.size(), is(0))
    }

    static List<Deviation> createDeviationList() {
        List<Deviation> devList = []
        devList << [lineNumbers: ["1", "2", "3"], transportMode: TransportMode.TRAIN, header: "First"]
        devList << [lineNumbers: ["1"], transportMode: TransportMode.TRAIN, header: "Second"]
        devList << [lineNumbers: ["3"], transportMode: TransportMode.BUS, header: "Third"]
        return devList
    }

}
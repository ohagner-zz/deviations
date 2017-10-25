package com.ohagner.deviations

import com.ohagner.deviations.api.deviation.service.DeviationMatcher
import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.transport.domain.Transport

import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.notification.domain.NotificationType
import com.ohagner.deviations.api.watch.domain.schedule.SingleOccurrence
import com.ohagner.deviations.worker.watch.service.WatchProcessor
import com.ohagner.deviations.worker.api.service.DeviationsApiClient
import com.ohagner.deviations.worker.watch.domain.WatchProcessingResult
import com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus
import groovy.util.logging.Slf4j
import spock.lang.Ignore
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import static com.ohagner.deviations.config.Constants.ZONE_ID

/**
 * Test job for matching watches against current deviations
 */
@Slf4j
@Ignore
class WatchProcessorSpec extends Specification {

    DeviationsApiClient client = Mock()
    WatchProcessor watchProcessor

    def setup() {
        watchProcessor  = new WatchProcessor(deviationMatcher: new DeviationMatcher(createDeviationList()), deviationsApiClient: client)
    }

    def 'should match deviations'() {
        given:
            def matchingWatch = createMatching()
        when:
            WatchProcessingResult result = watchProcessor.process(matchingWatch)
        then:
            1 * client.sendNotifications(_,_) >> true
            1 * client.update(_) >> true
            assert result.status == WatchProcessingStatus.NOTIFIED
            assert result.matchingDeviations.size() == 1
    }

    def 'should not be time to check'() {
        given:
            def matchingWatch = createMatching()
             matchingWatch.schedule = new SingleOccurrence(timeOfEvent: LocalTime.now(ZONE_ID).minusMinutes(5), dateOfEvent: LocalDate.now(ZONE_ID))
        when:
            WatchProcessingResult result = watchProcessor.process(matchingWatch)
        then:
            0 * client.sendNotifications(_,_)
            1 * client.update(_) >> true
            assert result.status == WatchProcessingStatus.NOT_TIME_TO_CHECK
            assert result.matchingDeviations.size() == 0
    }

    def 'should not match deviations'() {
        given:
            def nonMatchingWatch = createNonMatching()
        when:
            WatchProcessingResult result = watchProcessor.process(nonMatchingWatch)
        then:
            0 * client.sendNotifications(_,_)
            1 * client.update(_) >> true
            assert result.status == WatchProcessingStatus.NO_MATCH
            assert result.matchingDeviations.size() == 0
    }

    def 'should fail notifying user'() {
        given:
            def matchingWatch = createMatching()
        when:
            WatchProcessingResult result = watchProcessor.process(matchingWatch)
        then:
            1 * client.sendNotifications(_,_) >> false
            0 * client.update(_)
            assert result.status == WatchProcessingStatus.NOTIFICATION_FAILED
            assert result.matchingDeviations.size() == 1
    }

    def 'should fail updating watch'() {
        given:
            def matchingWatch = createMatching()
        when:
            WatchProcessingResult result = watchProcessor.process(matchingWatch)
        then:
            1 * client.sendNotifications(_,_) >> true
            1 * client.update(_) >> false
            assert result.status == WatchProcessingStatus.UPDATE_FAILED
            assert result.matchingDeviations.size() == 1
    }

    List<Deviation> createDeviationList() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID)
        def deviation = new Deviation()
        deviation.id = 1
        deviation.header = "header"
        deviation.lineNumbers = ["35", "36"]
        deviation.transportMode = TransportMode.BUS
        deviation.details = "details"
        deviation.from = now
        deviation.to = now.plusHours(1)
        deviation.created = now
        deviation.updated = now
        return [deviation]
    }


    private Watch createMatching() {
        def matching = new Watch(name: "matching", notifyMaxHoursBefore: 1, notifyBy: [NotificationType.LOG], transports: [new Transport(line: "35", transportMode: TransportMode.BUS)])
        matching.schedule = new SingleOccurrence(timeOfEvent: LocalTime.now(ZONE_ID).plusMinutes(5), dateOfEvent: LocalDate.now(ZONE_ID))
        return matching
    }

    private Watch createNonMatching() {
        def nonMatching = new Watch(name: "nonMatching", notifyMaxHoursBefore: 1, notifyBy: [NotificationType.LOG], transports: [new Transport(line: "99", transportMode: TransportMode.TRAIN)])
        nonMatching.schedule = new SingleOccurrence(timeOfEvent: LocalTime.now(ZONE_ID).plusMinutes(5l), dateOfEvent: LocalDate.now(ZONE_ID))
        return nonMatching
    }

}


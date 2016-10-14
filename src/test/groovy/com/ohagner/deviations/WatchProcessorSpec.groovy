package com.ohagner.deviations

import com.ohagner.deviations.domain.Deviation
import com.ohagner.deviations.domain.Transport
import com.ohagner.deviations.domain.TransportMode
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.domain.notifications.NotificationType
import com.ohagner.deviations.domain.schedule.SingleOccurrence
import com.ohagner.deviations.testutils.MockHTTPClient
import com.ohagner.deviations.watch.WatchProcessor
import com.ohagner.deviations.watch.task.WatchProcessingResult
import com.ohagner.deviations.watch.task.WatchProcessingStatus
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import ratpack.http.client.internal.DefaultHttpClient
import ratpack.test.http.internal.DefaultTestHttpClient
import spock.lang.Specification
import wslite.http.HTTPClient
import wslite.http.HTTPRequest
import wslite.http.HTTPResponse
import wslite.rest.RESTClient

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import static com.ohagner.deviations.config.Constants.ZONE_ID

/**
 * Test job for matching watches against current deviations
 */
@Slf4j
class WatchProcessorSpec extends Specification {

    RESTClient client
    WatchProcessor watchProcessor

    def setup() {
        client = new RESTClient('http://test.com')
        def httpClient = new MockHTTPClient()
        httpClient.responses = [getMockResponse(), getMockResponse()] as Queue
        client.httpClient = httpClient
        watchProcessor  = new WatchProcessor(deviationMatcher: new DeviationMatcher(createDeviationList()), deviationsApiClient: client)
    }

    def 'should match deviations'() {
        given:
            def matchingWatch = createMatching()
        when:
            WatchProcessingResult result = watchProcessor.process(matchingWatch)
        then:
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
            assert result.status == WatchProcessingStatus.NOT_TIME_TO_CHECK
            assert result.matchingDeviations.size() == 0
    }

    def 'should not match deviations'() {
        given:
            def nonMatchingWatch = createNonMatching()
        when:
            WatchProcessingResult result = watchProcessor.process(nonMatchingWatch)
        then:
            assert result.status == WatchProcessingStatus.NO_MATCH
            assert result.matchingDeviations.size() == 0
    }

    def 'should fail notifying user'() {
        given:
            def matchingWatch = createMatching()
            def httpClient = new MockHTTPClient()
            httpClient.responses = [getMockResponse(500), getMockResponse(200)] as Queue
            client.httpClient = httpClient
        when:
            WatchProcessingResult result = watchProcessor.process(matchingWatch)
        then:
            assert result.status == WatchProcessingStatus.NOTIFICATION_FAILED
            assert result.matchingDeviations.size() == 1
    }

    def 'should fail updating watch'() {
        given:
            def matchingWatch = createMatching()
            def httpClient = new MockHTTPClient()
            httpClient.responses = [getMockResponse(200), getMockResponse(500)] as Queue
            client.httpClient = httpClient
        when:
            WatchProcessingResult result = watchProcessor.process(matchingWatch)
        then:
            assert result.status == WatchProcessingStatus.WATCH_UPDATE_FAILED
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

    private HTTPResponse getMockResponse(statusCode=200, data=null) {
        def response = new HTTPResponse()
        response.statusCode = statusCode
        response.statusMessage = 'OK'
        response.headers = [:]
        response.data = data
        response.contentEncoding = 'UTF-8'
        response.contentType = 'application/json'
        response.contentLength = data ? data.size() : 0
        return response
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


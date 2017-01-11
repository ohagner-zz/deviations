package com.ohagner.deviations.domain

import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.domain.schedule.Schedule
import com.ohagner.deviations.api.watch.domain.schedule.SingleOccurrence
import com.ohagner.deviations.api.watch.domain.schedule.WeeklySchedule
import com.ohagner.deviations.api.transport.domain.Transport
import net.javacrumbs.jsonunit.core.Option
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import static com.ohagner.deviations.api.notification.domain.NotificationType.*
import static groovy.util.GroovyTestCase.assertEquals
import static java.time.DayOfWeek.FRIDAY
import static java.time.DayOfWeek.MONDAY
import static org.hamcrest.MatcherAssert.assertThat
import static net.javacrumbs.jsonunit.JsonMatchers.*


class WatchSpec extends Specification {

    def 'transform watch with weekly schedule to json'() {
        given:
            String expected = new File("src/test/resources/watches/weeklyScheduleWatch.json").text
            Watch watch = createWeeklyScheduleWatch()
        expect:
            assertThat(watch.toJson(), jsonEquals(expected).when(Option.IGNORING_ARRAY_ORDER))
    }

    def 'transform weekly schedule watch json to object'() {
        given:
            Watch watch = Watch.fromJson(new File("src/test/resources/watches/weeklyScheduleWatch.json").text)
        expect:
            Watch expected = createWeeklyScheduleWatch()
            assertEquals(watch, expected)
    }

    def 'transform watch with single occurrence schedule to json'() {
        given:
            Watch watch = createSingleOccurrenceWatch()
        expect:
            String expected = new File("src/test/resources/watches/singleOccurrenceWatch.json").text
            assertThat(watch.toJson(), jsonEquals(expected).when(Option.IGNORING_ARRAY_ORDER))
    }

    def 'transform single occurrence schedule watch json to object'() {
        given:
            Watch watch = Watch.fromJson(new File("src/test/resources/watches/singleOccurrenceWatch.json").text)
        expect:
            Watch expected = createSingleOccurrenceWatch()
            assertEquals(watch, expected)
    }

    def 'assert processed deviations queue behaviour'() {
        given:
            Watch watch = new Watch()
        when:
            watch.addProcessedDeviationIds((1..20).toList())
        then:
            assert watch.processedDeviationIds.size() == 20
        and:
        when:
            watch.addProcessedDeviationIds([21, 22])
        then:
            assert watch.processedDeviationIds.size() == 20
            assert watch.processedDeviationIds.containsAll((3..22).toList())
            assert watch.processedDeviationIds.contains(1) == false
            assert watch.processedDeviationIds.contains(2) == false

    }


    private Watch createWeeklyScheduleWatch() {
        Schedule schedule = new WeeklySchedule(weekDays: MONDAY..FRIDAY, timeOfEvent: LocalTime.of(10,45))
        LocalDateTime date = LocalDateTime.of(2016,10,10, 10, 10)
        List<Transport> transports = [new Transport(line:"35", transportMode: Deviation.TransportMode.TRAIN), new Transport(line:"807B", transportMode: Deviation.TransportMode.BUS)]
        return new Watch(name:"name", username: "username", notifyMaxHoursBefore: 2, schedule: schedule, notifyBy: [SLACK, EMAIL, LOG, WEBHOOK], created: date, lastProcessed: date, transports: transports)
    }

    private Watch createSingleOccurrenceWatch() {
        Schedule schedule = new SingleOccurrence(dateOfEvent: LocalDate.of(2016,10,10), timeOfEvent: LocalTime.of(10,10))
        LocalDateTime date = LocalDateTime.of(2016,10,10, 10, 10)
        Queue<String> processedDeviationIds = new LinkedList(["1", "2"])
        List<Transport> transports = [new Transport(line:"35", transportMode: Deviation.TransportMode.TRAIN), new Transport(line:"807B", transportMode: Deviation.TransportMode.BUS)]
        return new Watch(id:99,name:"name", username: "username", notifyMaxHoursBefore: 2, schedule: schedule, notifyBy: [EMAIL, LOG], created: date, lastProcessed: date, transports: transports, processedDeviationIds: processedDeviationIds )
    }

}

package com.ohagner.deviations.domain

import com.ohagner.deviations.domain.schedule.Schedule
import com.ohagner.deviations.domain.schedule.SingleOccurrence
import com.ohagner.deviations.domain.schedule.WeeklySchedule
import groovy.json.JsonOutput
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import static com.ohagner.deviations.domain.notifications.NotificationType.*
import static groovy.util.GroovyTestCase.assertEquals
import static java.time.DayOfWeek.FRIDAY
import static java.time.DayOfWeek.MONDAY
import static junit.framework.TestCase.assertTrue
import static org.hamcrest.MatcherAssert.assertThat
import static net.javacrumbs.jsonunit.JsonAssert.*
import static net.javacrumbs.jsonunit.JsonMatchers.*;


class WatchSpec extends Specification {

    def 'transform watch with weekly schedule to json'() {
        given:
            Watch watch = createWeeklyScheduleWatch()
        expect:
        String expected = new File("src/test/resources/watches/weeklyScheduleWatch.json").text
        assertThat(watch.toJson(), jsonEquals(expected))
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
            assertThat(watch.toJson(), jsonEquals(expected))
    }

    def 'transform single occurrence schedule watch json to object'() {
        given:
            Watch watch = Watch.fromJson(new File("src/test/resources/watches/singleOccurrenceWatch.json").text)
        expect:
            Watch expected = createSingleOccurrenceWatch()
            assertEquals(watch, expected)
    }

    private Watch createWeeklyScheduleWatch() {
        Schedule schedule = new WeeklySchedule(weekDays: MONDAY..FRIDAY, timeOfEvent: LocalTime.of(10,45))
        LocalDateTime date = LocalDateTime.of(2016,10,10, 10, 10)
        List<Transport> transports = [new Transport(line:"35", transportMode: TransportMode.TRAIN), new Transport(line:"807B", transportMode: TransportMode.BUS)]
        return new Watch(name:"name", user: "user", notifyMaxHoursBefore: 2, schedule: schedule, notifications: [EMAIL, LOG], created: date, lastUpdated: date, transports: transports)
    }

    private Watch createSingleOccurrenceWatch() {
        Schedule schedule = new SingleOccurrence(dateOfEvent: LocalDate.of(2016,10,10), timeOfEvent: LocalTime.of(10,10))
        LocalDateTime date = LocalDateTime.of(2016,10,10, 10, 10)
        List<Transport> transports = [new Transport(line:"35", transportMode: TransportMode.TRAIN), new Transport(line:"807B", transportMode: TransportMode.BUS)]
        return new Watch(name:"name", user: "user", notifyMaxHoursBefore: 2, schedule: schedule, notifications: [EMAIL, LOG], created: date, lastUpdated: date, transports: transports)
    }

}

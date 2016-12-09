package com.ohagner.deviations.domain.schedule

import com.ohagner.deviations.api.watch.schedule.Schedule
import com.ohagner.deviations.api.watch.schedule.SingleOccurrence
import com.ohagner.deviations.api.watch.schedule.WeeklySchedule
import groovy.json.JsonOutput
import spock.lang.Specification

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

import static junit.framework.Assert.assertEquals
import static org.hamcrest.CoreMatchers.hasItems
import static org.hamcrest.MatcherAssert.assertThat

class ScheduleSpec extends Specification {

    def 'create weekly schedule type from json'() {
        given:
            String json = JsonOutput.toJson {
                type "WEEKLY"
                weekdays(["MONDAY", "TUESDAY"])
                timeOfEvent "20:04"
            }
        when:
            WeeklySchedule schedule = Schedule.fromJson(json)
        then:
            assertThat(schedule.weekDays, hasItems(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
            assertEquals(schedule.timeOfEvent, LocalTime.of(20,04))
    }

    def 'create single schedule type from json'() {
        given:
            String json = JsonOutput.toJson {
                type "SINGLE"
                dateOfEvent "2016-09-15"
                timeOfEvent "20:04"
            }
        when:
            SingleOccurrence schedule = Schedule.fromJson(json)
        then:
            assertEquals(schedule.dateOfEvent, LocalDate.of(2016, 9, 15))
            assertEquals(schedule.timeOfEvent, LocalTime.of(20,04))
    }


}

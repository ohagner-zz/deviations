package com.ohagner.deviations.domain.schedule

import com.ohagner.deviations.api.watch.schedule.WeeklySchedule
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import static java.time.DayOfWeek.FRIDAY
import static java.time.DayOfWeek.MONDAY


class WeeklyScheduleSpec extends Specification {

    final static LocalDate THURSDAY_DATE = LocalDate.of(2016, 05, 12)

    def "correct weekday and time"() {
        given:
            def monToFriScheduleAt1045 = new WeeklySchedule(weekDays: MONDAY..FRIDAY, timeOfEvent: LocalTime.of(10,45))
            def timeToCheck = LocalDateTime.of(THURSDAY_DATE, LocalTime.of(8,46))
        expect:
            monToFriScheduleAt1045.isEventWithinPeriod(timeToCheck, 2)
    }

    def "should be time to check when event is just after midnight"() {
        given:
            def monToFriScheduleAt1045 = new WeeklySchedule(weekDays: MONDAY..FRIDAY, timeOfEvent: LocalTime.of(0,45))
            def timeToCheck = LocalDateTime.of(THURSDAY_DATE, LocalTime.of(23,59))
        expect:
            monToFriScheduleAt1045.isEventWithinPeriod(timeToCheck, 1)
    }

    def "correct weekday, time just after event"() {
        given:
            def monToFriScheduleAt1045 = new WeeklySchedule(weekDays: MONDAY..FRIDAY, timeOfEvent: LocalTime.of(8,45))
            def timeToCheck = LocalDateTime.of(THURSDAY_DATE, LocalTime.of(8,46))
        expect:
            monToFriScheduleAt1045.isEventWithinPeriod(timeToCheck, 2) == false
    }

    def "correct weekday, time just before event"() {
        given:
            def monToFriScheduleAt1045 = new WeeklySchedule(weekDays: MONDAY..FRIDAY, timeOfEvent: LocalTime.of(8,45))
            def timeToCheck = LocalDateTime.of(THURSDAY_DATE, LocalTime.of(8,44))
        expect:
            monToFriScheduleAt1045.isEventWithinPeriod(timeToCheck, 2)
    }



}


package com.ohagner.deviations.domain.schedule

import com.ohagner.deviations.api.watch.domain.schedule.SingleOccurrence
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SingleOccurrenceSpec extends Specification {

    def 'time just before and right date'() {
        given:
            LocalTime timeOfEvent = LocalTime.of(9,55)
            LocalDate dateOfEvent = LocalDate.of(2016,10,10)
            SingleOccurrence singleOccurrence = new SingleOccurrence(dateOfEvent: dateOfEvent, timeOfEvent: timeOfEvent)
            LocalDateTime timeToCheck = LocalDateTime.of(dateOfEvent, timeOfEvent.minusMinutes(1))
        expect:
            singleOccurrence.isEventWithinPeriod(timeToCheck, 1)
    }

    def 'right time and wrong date'() {
        given:
            LocalTime timeOfEvent = LocalTime.of(9,55)
            LocalDate dateOfEvent = LocalDate.of(2016,10,10)
            SingleOccurrence singleOccurrence = new SingleOccurrence(dateOfEvent: dateOfEvent, timeOfEvent: timeOfEvent)
            LocalDateTime timeToCheck = LocalDateTime.of(dateOfEvent.plusDays(1), timeOfEvent.minusMinutes(30))
        expect:
            singleOccurrence.isEventWithinPeriod(timeToCheck, 1) == false
    }


    def 'time just after and right date'() {
        given:
            LocalTime timeOfEvent = LocalTime.of(9,55)
            LocalDate dateOfEvent = LocalDate.of(2016,10,10)
            SingleOccurrence singleOccurrence = new SingleOccurrence(dateOfEvent: dateOfEvent, timeOfEvent: timeOfEvent)
            LocalDateTime timeToCheck = LocalDateTime.of(dateOfEvent, timeOfEvent.plusMinutes(1))
        expect:
            singleOccurrence.isEventWithinPeriod(timeToCheck, 1) == false
    }

    def 'the exact same time'() {
        given:
            LocalTime timeOfEvent = LocalTime.of(9,55)
            LocalDate dateOfEvent = LocalDate.of(2016,10,10)
            SingleOccurrence singleOccurrence = new SingleOccurrence(dateOfEvent: dateOfEvent, timeOfEvent: timeOfEvent)
            LocalDateTime timeToCheck = LocalDateTime.of(dateOfEvent, timeOfEvent)
        expect:
            singleOccurrence.isEventWithinPeriod(timeToCheck, 1) == false
    }

    def 'just after midnight'() {
        given:
            LocalTime timeOfEvent = LocalTime.of(1,0)
            LocalDate dateOfEvent = LocalDate.of(2016,10,10)
            SingleOccurrence singleOccurrence = new SingleOccurrence(dateOfEvent: dateOfEvent, timeOfEvent: timeOfEvent)
            LocalDateTime timeToCheck = LocalDateTime.of(dateOfEvent, LocalTime.of(0,15))
        expect:
            singleOccurrence.isEventWithinPeriod(timeToCheck, 2) == true
    }


}

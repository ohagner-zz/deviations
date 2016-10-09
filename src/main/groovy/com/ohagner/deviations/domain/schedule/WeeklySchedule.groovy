package com.ohagner.deviations.domain.schedule

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.TupleConstructor

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.*

@TupleConstructor(force = true)
class WeeklySchedule extends Schedule {

    List<DayOfWeek> weekDays

    @JsonFormat(pattern="HH:mm", shape=STRING)
    LocalTime timeOfEvent

    @JsonCreator
    WeeklySchedule(@JsonProperty("timeOfEvent") String timeOfEventString, @JsonProperty("weekdays")List<DayOfWeek> weekDays) {
        def timeParts = timeOfEventString.split(":").collect { Integer.parseInt(it)}
        this.timeOfEvent = LocalTime.of(timeParts[0], timeParts[1])
        this.weekDays = weekDays
    }

    @Override
    boolean isEventWithinPeriod(LocalDateTime now, long hoursBefore) {
        boolean scheduleHasWeekday = weekDays.contains(now.dayOfWeek)
        LocalTime timeNow = now.toLocalTime()
        boolean isWithinTime = timeOfEvent.minusHours(hoursBefore).isBefore(timeNow) && timeOfEvent.isAfter(timeNow)
        return scheduleHasWeekday && isWithinTime
    }

    @Override
    boolean isTimeToArchive() {
        return false
    }

    @JsonProperty("timeToArchive")
    void setTimeToArchive(String timeToArchive) {}

//    @JsonProperty
//    String getType() {
//        return "WEEKLYHEJ"
//    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        WeeklySchedule that = (WeeklySchedule) o

        if (timeOfEvent != that.timeOfEvent) return false
        if (weekDays != that.weekDays) return false

        return true
    }

    int hashCode() {
        int result
        result = (weekDays != null ? weekDays.hashCode() : 0)
        result = 31 * result + (timeOfEvent != null ? timeOfEvent.hashCode() : 0)
        return result
    }
}

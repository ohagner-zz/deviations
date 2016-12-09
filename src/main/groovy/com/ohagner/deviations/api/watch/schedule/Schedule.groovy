package com.ohagner.deviations.api.watch.schedule

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import java.time.LocalDateTime

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes([
        @JsonSubTypes.Type(value = SingleOccurrence.class, name = "SINGLE"),
        @JsonSubTypes.Type(value = WeeklySchedule.class, name = "WEEKLY")
])
@Slf4j
class Schedule {


    boolean isEventWithinPeriod(LocalDateTime now, long hoursBefore) { return false }

    boolean isTimeToArchive() { return true }

    static Schedule fromJson(String json){
        ObjectMapper mapper = new ObjectMapper()
        def schedule = new JsonSlurper().parseText(json)

        if(schedule.type == 'WEEKLY') {
            return mapper.readValue(json, WeeklySchedule.class)
        } else if(schedule.type == 'SINGLE') {
            return mapper.readValue(json, SingleOccurrence.class)
        } else {
            throw new IllegalArgumentException("Invalid schedule type")
        }
    }

}

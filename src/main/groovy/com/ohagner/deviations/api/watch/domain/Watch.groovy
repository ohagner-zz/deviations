package com.ohagner.deviations.api.watch.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.ohagner.deviations.config.Constants

import com.ohagner.deviations.api.notification.domain.NotificationType
import com.ohagner.deviations.api.watch.domain.schedule.Schedule
import com.ohagner.deviations.api.transport.domain.Transport
import groovy.transform.CompileStatic

import java.time.LocalDateTime

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
class Watch {

    private static final ObjectMapper mapper
    public static final int MAXIMUM_PROCESSED_DEVIATIONS_LIST_SIZE = 20
    static {
        mapper = new ObjectMapper()
        mapper.findAndRegisterModules()
    }

    Long id

    String name

    String username

    int notifyMaxHoursBefore

    Schedule schedule

    List<Transport> transports

    List<NotificationType> notifyBy

    Queue<String> processedDeviationIds = new LinkedList<String>()


    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime lastNotified

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime created

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime lastProcessed

    static Watch fromJson(String json) {
        return mapper.readValue(json, Watch)
    }

    String toJson() {
        return mapper.writeValueAsString(this)
    }

    @CompileStatic
    void addProcessedDeviationIds(Collection<String> deviationIds) {
        deviationIds.each { processedDeviationIds.add(it) }
        while(processedDeviationIds.size() > MAXIMUM_PROCESSED_DEVIATIONS_LIST_SIZE) {
            processedDeviationIds.remove()
        }
    }

    @JsonIgnore
    boolean isTimeToCheck(LocalDateTime now) {
        return schedule.isEventWithinPeriod(now, notifyMaxHoursBefore)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Watch watch = (Watch) o
        if (id != watch.id) return false
        if (notifyMaxHoursBefore != watch.notifyMaxHoursBefore) return false
        if (created != watch.created) return false
        if (name != watch.name) return false
        if (notifyBy != null && !notifyBy.containsAll(watch.notifyBy)) return false
        if (schedule != watch.schedule) return false
        if (transports != watch.transports) return false
        if (username != watch.username) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (username != null ? username.hashCode() : 0)
        result = 31 * result + notifyMaxHoursBefore
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0)
        result = 31 * result + (transports != null ? transports.hashCode() : 0)
        result = 31 * result + (notifyBy != null ? notifyBy.hashCode() : 0)
        result = 31 * result + (created != null ? created.hashCode() : 0)
        return result
    }
}

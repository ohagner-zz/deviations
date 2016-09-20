package com.ohagner.deviations.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.ohagner.deviations.config.DateConstants
import com.ohagner.deviations.domain.notifications.NotificationType
import com.ohagner.deviations.domain.schedule.Schedule

import java.time.LocalDateTime

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Watch {

    private static final ObjectMapper mapper
    static {
        mapper = new ObjectMapper()
        mapper.findAndRegisterModules()
    }

    String name
    String user

    int notifyMaxHoursBefore

    Schedule schedule

    List<Transport> transports

    List<NotificationType> notifications

    @JsonFormat(pattern=DateConstants.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime created

    @JsonFormat(pattern=DateConstants.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime lastUpdated

    static Watch fromJson(String json) {
        return mapper.readValue(json, Watch)
    }

    String toJson() {
        return mapper.writeValueAsString(this)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Watch watch = (Watch) o

        if (notifyMaxHoursBefore != watch.notifyMaxHoursBefore) return false
        if (created != watch.created) return false
        if (name != watch.name) return false
        if (notifications != watch.notifications) return false
        if (schedule != watch.schedule) return false
        if (transports != watch.transports) return false
        if (user != watch.user) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (user != null ? user.hashCode() : 0)
        result = 31 * result + notifyMaxHoursBefore
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0)
        result = 31 * result + (transports != null ? transports.hashCode() : 0)
        result = 31 * result + (notifications != null ? notifications.hashCode() : 0)
        result = 31 * result + (created != null ? created.hashCode() : 0)
        return result
    }
}

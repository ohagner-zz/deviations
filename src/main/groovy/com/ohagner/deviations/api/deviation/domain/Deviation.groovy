package com.ohagner.deviations.api.deviation.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.ohagner.deviations.api.deviation.service.LineNumberParser
import com.ohagner.deviations.config.Constants

import groovy.json.JsonSlurper
import groovy.transform.builder.Builder

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
class Deviation {

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()

    String id

    String header

    String details

    List<String> lineNumbers

    TransportMode transportMode

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime from

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime to

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime created

    @JsonFormat(pattern=Constants.Date.LONG_DATE_FORMAT, shape=STRING)
    LocalDateTime updated

    static Deviation fromJson(String jsonString, TransportMode transportMode) {
        def json = new JsonSlurper().parseText(jsonString)

        Deviation instance = new Deviation(transportMode: transportMode)
        instance.id = json.DevCaseGid
        instance.header = json.Header
        instance.details = json.Details
        instance.details = instance.details.replaceAll(/(\n)+\s?/, "\n")
        instance.lineNumbers = new LineNumberParser().extractLineNumbers(json.ScopeElements)
        instance.from = LocalDateTime.parse(json.FromDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        instance.to = LocalDateTime.parse(json.UpToDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        instance.created = LocalDateTime.parse(json.Created, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        instance.updated = LocalDateTime.parse(json.Updated, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        return instance
    }

    String toJson() {
        return mapper.writeValueAsString(this)
    }

    @JsonIgnore
    Duration getDuration() {
        return Duration.between(from, to)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Deviation deviation = (Deviation) o

        if (created != deviation.created) return false
        if (details != deviation.details) return false
        if (from != deviation.from) return false
        if (header != deviation.header) return false
        if (id != deviation.id) return false
        if (lineNumbers != deviation.lineNumbers) return false
        if (to != deviation.to) return false
        if (transportMode != deviation.transportMode) return false
        if (updated != deviation.updated) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (header != null ? header.hashCode() : 0)
        result = 31 * result + (details != null ? details.hashCode() : 0)
        result = 31 * result + (lineNumbers != null ? lineNumbers.hashCode() : 0)
        result = 31 * result + (transportMode != null ? transportMode.hashCode() : 0)
        result = 31 * result + (from != null ? from.hashCode() : 0)
        result = 31 * result + (to != null ? to.hashCode() : 0)
        result = 31 * result + (created != null ? created.hashCode() : 0)
        result = 31 * result + (updated != null ? updated.hashCode() : 0)
        return result
    }

    @Override
    public String toString() {
        return "Deviation{" +
                "header='" + header + '\'' +
                ", lineNumbers=" + lineNumbers +
                ", transportMode=" + transportMode +
                ", from=" + from +
                ", to=" + to +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }

    static enum TransportMode {
        TRAIN, BUS, SUBWAY
    }
}

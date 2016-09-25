package com.ohagner.deviations

import com.fasterxml.jackson.databind.ObjectMapper
import com.ohagner.deviations.domain.TransportMode
import com.ohagner.deviations.parser.LineNumberParser
import groovy.json.JsonSlurper
import groovy.transform.builder.Builder

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Builder
class Deviation {

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()

    String header
    String details

    List<String> lineNumbers
    TransportMode transportMode
    LocalDateTime from
    LocalDateTime to
    LocalDateTime created
    LocalDateTime updated

    static Deviation fromJson(String jsonString, TransportMode transportMode) {
        def json = new JsonSlurper().parseText(jsonString)

        Deviation instance = new Deviation(transportMode: transportMode)
        instance.header = json.Header
        instance.details = json.Details
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


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Deviation deviation = (Deviation) o

        if (created != deviation.created) return false
        if (details != deviation.details) return false
        if (from != deviation.from) return false
        if (header != deviation.header) return false
        if (lineNumbers != deviation.lineNumbers) return false
        if (to != deviation.to) return false
        if (transportMode != deviation.transportMode) return false
        if (updated != deviation.updated) return false

        return true
    }

    int hashCode() {
        int result
        result = header.hashCode()
        result = 31 * result + details.hashCode()
        result = 31 * result + lineNumbers.hashCode()
        result = 31 * result + transportMode.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + created.hashCode()
        result = 31 * result + updated.hashCode()
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
}

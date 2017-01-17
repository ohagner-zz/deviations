package com.ohagner.deviations.api.notification.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.ohagner.deviations.api.deviation.domain.Deviation
import groovy.transform.CompileStatic

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Notification {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private static final ObjectMapper mapper = new ObjectMapper()

    @JsonIgnore
    List<NotificationType> notificationTypes

    String header
    String message

    String toJson() {
        return mapper.writeValueAsString(this)
    }

    static Notification fromJson(String json) {
        return mapper.readValue(json, Notification)
    }

    @CompileStatic
    static Notification fromDeviations(Collection<Deviation> deviations, List<NotificationType> notificationTypes) {

        String message = deviations.collect { Deviation deviation ->
        """
            Linjer: ${deviation.lineNumbers?.join(",")}
            Rubrik: ${deviation.header}
            Detaljer: ${deviation.details}
            Gäller från: ${formatDate(deviation.from)}
            Gäller till: ${formatDate(deviation.to)}
            Skapad: ${formatDate(deviation.created)}
            Uppdaterad: ${formatDate(deviation.updated)}
        """
        }.join("\n")

        return new Notification(notificationTypes: notificationTypes, header: "Trafikbevakningsinformation", message: message)
    }

    @CompileStatic
    static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(dateTimeFormatter)
    }

}

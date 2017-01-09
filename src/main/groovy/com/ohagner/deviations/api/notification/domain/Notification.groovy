package com.ohagner.deviations.api.notification.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.ohagner.deviations.api.deviation.domain.Deviation
import groovy.transform.CompileStatic


class Notification {

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

        String message = deviations.collect { deviation ->
        """
            Linjer: ${deviation.lineNumbers.join(",")}
            Rubrik: ${deviation.header}
            Detaljer: ${deviation.details}
            Gäller från: ${deviation.from}
            Gäller till: ${deviation.to}
            Uppdaterad: ${deviation.updated}
        """
        }.join("\n")

        return new Notification(notificationTypes: notificationTypes, header: "Trafikbevakningsinformation", message: message)
    }



}

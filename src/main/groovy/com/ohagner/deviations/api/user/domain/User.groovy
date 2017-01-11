package com.ohagner.deviations.api.user.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@TupleConstructor(force = true)
class User {

    private static final ObjectMapper mapper
    static {
        mapper = new ObjectMapper()
        mapper.findAndRegisterModules()
    }

    String firstName
    String lastName
    String emailAddress

    Credentials credentials

    Webhook webhook
    Webhook slackWebhook

    static User fromJson(String json) {
        return mapper.readValue(json, User)
    }

    String toJson() {
        return mapper.writeValueAsString(this)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        User user = (User) o

        if (credentials != user.credentials) return false
        if (emailAddress != user.emailAddress) return false
        if (firstName != user.firstName) return false
        if (lastName != user.lastName) return false
        if (webhook != user.webhook) return false
        if (slackWebhook != user.slackWebhook) return false
        return true
    }

    int hashCode() {
        int result
        result = (firstName != null ? firstName.hashCode() : 0)
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0)
        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0)
        result = 31 * result + (credentials != null ? credentials.hashCode() : 0)
        result = 31 * result + (webhook != null ? webhook.hashCode() : 0)
        result = 31 * result + (slackWebhook != null ? slackWebhook.hashCode() : 0)
        return result
    }

    @JsonIgnore
    boolean isAdministrator() {
        return this?.credentials?.role == Role.ADMIN
    }
}

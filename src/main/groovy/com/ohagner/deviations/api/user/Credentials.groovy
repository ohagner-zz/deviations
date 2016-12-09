package com.ohagner.deviations.api.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder

@Builder
@TupleConstructor(force = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
class Credentials {

    String username

    String passwordHash

    String passwordSalt

    Token apiToken

    Role role

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Credentials that = (Credentials) o

        if (apiToken != that.apiToken) return false
        if (passwordHash != that.passwordHash) return false
        if (passwordSalt != that.passwordSalt) return false
        if (role != that.role) return false
        if (username != that.username) return false

        return true
    }

    int hashCode() {
        int result
        result = (username != null ? username.hashCode() : 0)
        result = 31 * result + (passwordHash != null ? passwordHash.hashCode() : 0)
        result = 31 * result + (passwordSalt != null ? passwordSalt.hashCode() : 0)
        result = 31 * result + (apiToken != null ? apiToken.hashCode() : 0)
        result = 31 * result + (role != null ? role.hashCode() : 0)
        return result
    }
}

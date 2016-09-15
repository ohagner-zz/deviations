package com.ohagner.deviations.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties("_id")
class User {
    final static ObjectMapper mapper = new ObjectMapper()

    String username
    String firstName
    String lastName
    String emailAddress

    static User fromJson(String json) {
        mapper.readValue(json, User)
    }

    String toJson() {
        return mapper.writeValueAsString(this)
    }
}

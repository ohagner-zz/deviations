package com.ohagner.deviations.domain

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class User {

    String firstName
    String lastName
    String emailAddress

}

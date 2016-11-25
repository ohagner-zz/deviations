package com.ohagner.deviations.domain.web

import groovy.transform.builder.Builder

@Builder
class WebUser implements Serializable {

    String username
    String firstName
    String lastName
    String emailAddress
    String apiToken

    private Map properties = [:]

    def propertyMissing(key) {
        return properties.get(key, "")
    }

    void propertyMissing(key, value) {
        properties.put(key, value)
    }
}

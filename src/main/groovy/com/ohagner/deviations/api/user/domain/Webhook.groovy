package com.ohagner.deviations.api.user.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Canonical

@Canonical
class Webhook {

    @JsonCreator
    public static Webhook newInstance(@JsonProperty("url") String urlstring) {
        try {
            URL url = new URL(urlstring)
            if(!["http", "https"].contains(url.protocol)) {
                throw new IllegalArgumentException("Only http or https URL:s are allowed")
            }
            return new Webhook(url)
        } catch(MalformedURLException) {
            throw new IllegalArgumentException("Invalid URL")
        }
    }

    private Webhook(URL url) {
        this.url = url
    }

    URL url

}

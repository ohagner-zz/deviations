package com.ohagner.deviations.api.error


class ConfigurationNotFoundException extends Exception {

    ConfigurationNotFoundException() {
    }

    ConfigurationNotFoundException(String message) {
        super(message)
    }
}

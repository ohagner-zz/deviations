package com.ohagner.deviations.api.error

class NotFoundException extends RuntimeException {

    NotFoundException(String message) {
        super(message)
    }
}

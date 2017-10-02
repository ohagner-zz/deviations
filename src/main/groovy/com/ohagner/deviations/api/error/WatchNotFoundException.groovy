package com.ohagner.deviations.api.error

class WatchNotFoundException extends NotFoundException {

    public WatchNotFoundException() {
    }

    public WatchNotFoundException(String message) {
        super(message)
    }
}

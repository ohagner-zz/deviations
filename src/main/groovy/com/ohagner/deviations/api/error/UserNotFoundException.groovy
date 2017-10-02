package com.ohagner.deviations.api.error

class UserNotFoundException extends NotFoundException {

    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message)
    }

}

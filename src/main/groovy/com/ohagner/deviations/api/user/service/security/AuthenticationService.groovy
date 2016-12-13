package com.ohagner.deviations.api.user.service.security

import com.ohagner.deviations.api.user.domain.User
import ratpack.exec.Promise

interface AuthenticationService {
    Promise<User> authenticate(String username, String password)

    Promise<User> authenticateAdministrator(String username, String password)

}
package com.ohagner.deviations.security

import com.ohagner.deviations.domain.user.User
import ratpack.exec.Promise

interface AuthenticationService {
    Promise<User> authenticate(String username, String password)

    Promise<User> authenticateAdministrator(String username, String password)

}
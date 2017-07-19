package com.ohagner.deviations.api.user.service.security

import com.google.inject.Inject
import com.ohagner.deviations.api.user.domain.Role
import com.ohagner.deviations.api.user.domain.Token
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import ratpack.exec.Promise

import java.time.LocalDate

import static com.ohagner.deviations.config.Constants.ZONE_ID

@CompileStatic
@Slf4j
class DefaultAuthenticationService implements AuthenticationService {

    private UserRepository userRepository

    @Inject
    DefaultAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    Promise<User> authenticate(String username, String password) {
        userRepository.findByUsername(username)
                .onNull { null }
                .map { User user ->
            String suppliedPasswordHash = HashGenerator.generateHash(password, user.credentials.passwordSalt)
            if (user.credentials.passwordHash == suppliedPasswordHash) {
                user.credentials.apiToken = new Token(value: UUID.randomUUID().toString(), expirationDate: LocalDate.now(ZONE_ID).plusWeeks(4))
                log.info "Newly generated token : ${user.credentials.apiToken.value}"
                userRepository.update(user.credentials.username, user)
                return user
            } else {
                log.debug "Supplied password did not match"
                return null
            }
        }
    }

    @Override
    Promise<User> authenticateAdministrator(String username, String password) {
        authenticate(username, password).map { User user ->
            user?.credentials?.role == Role.ADMIN ? user : null
        }
    }
}

package com.ohagner.deviations.security

import com.google.inject.Inject
import com.ohagner.deviations.Role
import com.ohagner.deviations.domain.user.Token
import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.repository.UserRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import ratpack.exec.Promise

import java.time.LocalDate

import static com.ohagner.deviations.config.Constants.ZONE_ID

@CompileStatic
@Slf4j
class AuthService {

    private UserRepository userRepository

    @Inject
    AuthService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    Promise<User> authenticate(String username, String password) {
        return Promise.sync {
            return updateTokenIfMatch(username, password).orElse(null)
        }
    }


    Promise<User> authenticateAdministrator(String username, String password) {

        return Promise.sync {
            updateTokenIfMatch(username, password)
                .filter { User user ->
                    user?.credentials?.role == Role.ADMIN
                }.orElse(null)
        }
    }

    Optional<User> updateTokenIfMatch(String username, String password) {
            userRepository.findByUsername(username)
            .filter { User user ->
                String passwordHash = HashGenerator.generateHash(password, user.credentials.passwordSalt)
                user?.credentials?.passwordHash == passwordHash
            }.map { User user->
                user.credentials.apiToken = new Token(value: UUID.randomUUID().toString(), expirationDate: LocalDate.now(ZONE_ID).plusWeeks(4))
                log.info "Newly generated token : ${user.credentials.apiToken.value}"
                userRepository.update(user.credentials.username, user)
                return user
            }

    }

}

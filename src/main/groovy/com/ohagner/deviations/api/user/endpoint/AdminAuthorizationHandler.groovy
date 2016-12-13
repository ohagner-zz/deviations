package com.ohagner.deviations.api.user.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.user.domain.Role
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import java.time.LocalDate

import static com.ohagner.deviations.config.Constants.Headers
import static com.ohagner.deviations.config.Constants.ZONE_ID
import static ratpack.jackson.Jackson.json

/**
 * Authorize user based on api security token
 */
@Slf4j
class AdminAuthorizationHandler extends GroovyHandler {

    UserRepository userRepository

    @Inject
    AdminAuthorizationHandler(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    protected void handle(GroovyContext context) {

        context.with {
            String suppliedApiToken = request.headers.get(Headers.USER_TOKEN)
            log.info "Supplied token: $suppliedApiToken"
            userRepository.findByApiToken(suppliedApiToken).onNull {
                log.warn "No user found for token ${suppliedApiToken}"
                response.status(401)
                render json([message: "Invalid api security token"])
            }.then { User user ->
                if (isValidToken(user, suppliedApiToken) && user.credentials?.role == Role.ADMIN) {
                    next()
                } else {
                    response.status(401)
                    render json([message: "Unauthorized"])
                }
            }
        }
    }

    private boolean isValidToken(User user, String suppliedToken) {
        return user?.credentials?.apiToken?.value == suppliedToken && user?.credentials?.apiToken?.expirationDate?.isAfter(LocalDate.now(ZONE_ID))
    }


}

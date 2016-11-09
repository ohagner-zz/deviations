package com.ohagner.deviations.handlers

import com.google.inject.Inject
import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.repository.UserRepository
import groovy.util.logging.Slf4j
import ratpack.exec.Blocking
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.registry.Registry

import java.time.LocalDate

import static com.ohagner.deviations.config.Constants.Headers
import static com.ohagner.deviations.config.Constants.ZONE_ID
import static ratpack.jackson.Jackson.json

/**
 * Authorize user based on api security token
 */
@Slf4j
class UserAuthorizationHandler extends GroovyHandler {

    UserRepository userRepository

    @Inject
    UserAuthorizationHandler(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    protected void handle(GroovyContext context) {

        context.with {
            String suppliedApiToken = request.headers.get(Headers.USER_TOKEN)
            String pathUsername = pathTokens.username

            userRepository.findByApiToken(suppliedApiToken)
                .onNull {
                    response.status(401)
                    render json([message: "Invalid api token"])
                }.then { User user ->
                    if (isValidToken(user, suppliedApiToken)) {
                        if(pathUsername != user.credentials.username) {
                            response.status(403)
                            render json([message: "Forbidden"])
                        } else {
                            log.info "Valid token, proceeding with next"
                            next(Registry.single(User, user))
                        }
                    } else {
                        response.status(401)
                        render json([message: "Api token has expired"])
                    }
                }
        }
    }

    private boolean isValidToken(User user, String suppliedToken) {
        return user?.credentials?.apiToken?.value == suppliedToken && user?.credentials?.apiToken?.expirationDate.isAfter(LocalDate.now(ZONE_ID))
    }


}

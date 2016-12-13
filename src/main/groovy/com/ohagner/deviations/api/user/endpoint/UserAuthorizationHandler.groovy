package com.ohagner.deviations.api.user.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import groovy.util.logging.Slf4j
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

    public static final String ERROR_MSG_USERNAME_MISMATCH = "Attempt to access other users data"
    public static final String ERROR_MSG_API_TOKEN_EXPIRED = "API token has expired"
    public static final String ERROR_MSG_INVALID_API_TOKEN = "Invalid api token"
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
                    render json([message: ERROR_MSG_INVALID_API_TOKEN])
                }.then { User user ->
                    if (isValidToken(user, suppliedApiToken)) {
                        if(pathUsername != user.credentials.username) {
                            response.status(403)
                            render json([message: ERROR_MSG_USERNAME_MISMATCH])
                        } else {
                            log.info "Valid token, proceeding with next"
                            next(Registry.single(User, user))
                        }
                    } else {
                        response.status(401)
                        render json([message: ERROR_MSG_API_TOKEN_EXPIRED])
                    }
                }
        }
    }

    private boolean isValidToken(User user, String suppliedToken) {
        return user?.credentials?.apiToken?.value == suppliedToken && user?.credentials?.apiToken?.expirationDate.isAfter(LocalDate.now(ZONE_ID))
    }


}

package com.ohagner.deviations.handlers

import com.google.inject.Inject
import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.security.AuthenticationService
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.jackson.Jackson.json

@Slf4j
class UserAuthenticationHandler extends GroovyHandler {

    AuthenticationService authenticationService

    @Inject
    UserAuthenticationHandler(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            request.body.then { body ->
                def jsonBody = new JsonSlurper().parseText(body.text)
                log.info "Authenticating with user ${jsonBody.username}"
                authenticationService.authenticate(jsonBody.username, jsonBody.password).onNull {
                    response.status(401)
                    render json([message: "Invalid username and/or password"])
                }.then { User user ->
                    response.status(200)
                    render user
                }
            }

        }
    }
}

package com.ohagner.deviations.handler

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.repository.UserRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.jackson.Jackson.json

@Slf4j
class UserHandler extends GroovyHandler {



    @Override
    void handle(GroovyContext ctx) throws Exception {
        ctx.with {
            byMethod {
                Optional<User> user = context.maybeGet(User)
                delete {
                    UserRepository userRepository = context.get(UserRepository)
                    if (user.isPresent()) {
                        log.info "Deleting user"
                        userRepository.delete(user.get())
                        render json(user.get())
                    } else {
                        log.info "NOT FOUND"
                        clientError(404)
                    }

                    render json(["message": "Delete user"])
                }
                get {
                    if (user.isPresent()) {
                        render json(user.get())
                    } else {
                        log.info "NOT FOUND"
                        clientError(404)
                    }
                }
                put {
                    render json(["message": "Update user"])
                }

            }
        }
    }
}

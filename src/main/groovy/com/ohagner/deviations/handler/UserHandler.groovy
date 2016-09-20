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
                        response.status(404)
                        render json(["message": "Not found"])
                    }
                }
                get {
                    if (user.isPresent()) {
                        render json(user.get())
                    } else {
                        log.info "USER NOT FOUND"
                        response.status(404)
                        render json(["message": "Not found"])
                    }
                }
                put {
                    UserRepository userRepository = context.get(UserRepository)
                    if(user.isPresent()) {
                        User current = user.get()
                        request.getBody().then {
                            String request = it.text
                            log.info "Request: $request"
                            User update = User.fromJson(request)

                            User updatedUser = userRepository.update(current.getUsername(), update)
                            render json(updatedUser)
                        }
                    } else {
                        log.info "USER NOT FOUND"
                        response.status(404)
                        render json(["message": "Not found"])
                    }
                }

            }
        }
    }
}

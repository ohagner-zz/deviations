package com.ohagner.deviations.handlers

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
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
                Optional<User> userOpt = context.maybeGet(User)
                if (!userOpt.present) {
                    log.debug "Failed to delete user, not found"
                    response.status(404)
                    render json(["message": "Not found"])
                }
                User existingUser = userOpt.get()
                delete {
                    UserRepository userRepository = context.get(UserRepository)
                    WatchRepository watchRepository = context.get(WatchRepository)
                    log.debug "Deleting user"
                    List<Watch> watches = watchRepository.findByUsername()
                    watches.each { Watch watch -> watchRepository.delete(existingUser.username, watch.name) }
                    userRepository.delete(existingUser)
                    render json(existingUser)

                }
                get {
                    render json(existingUser)
                }
                put {
                    UserRepository userRepository = context.get(UserRepository)
                    request.getBody().map {
                        String request = it.text
                        log.debug "User update request: $request"
                        User update = User.fromJson(request)
                        userRepository.update(existingUser.getUsername(), update)
                    }.onError { throwable ->
                        log.error("Failed to update user", throwable)
                        response.status(500)
                        render json([message: "Failed to update user"])
                    }.then { updatedUser ->
                        response.status(200)
                        render json(updatedUser)
                    }

                }
            }
        }
    }
}

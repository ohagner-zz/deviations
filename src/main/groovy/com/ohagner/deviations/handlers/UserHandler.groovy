package com.ohagner.deviations.handlers

import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.domain.user.User
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
            User user = get(User)
            byMethod {
                delete {
                    UserRepository userRepository = context.get(UserRepository)
                    WatchRepository watchRepository = context.get(WatchRepository)
                    log.debug "Deleting user"
                    List<Watch> watches = watchRepository.findByUsername(user.credentials.username)
                    watches.each { Watch watch -> watchRepository.delete(user.credentials.username, watch.name) }
                    userRepository.delete(user)
                    render user
                }
                get {
                    render user
                }
                put {
                    log.info "Updating user ${user.credentials.username}"
                    UserRepository userRepository = context.get(UserRepository)
                    request.getBody().map {
                        String request = it.text
                        log.info "User update request: $request"
                        User update = User.fromJson(request)
                        user.firstName = update.firstName
                        user.lastName = update.lastName
                        user.emailAddress = update.emailAddress
                        userRepository.update(user.credentials.username, user)
                    }.onError { throwable ->
                        log.error("Failed to update user", throwable)
                        response.status(500)
                        render json([message: "Failed to update user"])
                    }.then { updatedUser ->
                        response.status(200)
                        render updatedUser
                    }
                }
            }
        }
    }
}

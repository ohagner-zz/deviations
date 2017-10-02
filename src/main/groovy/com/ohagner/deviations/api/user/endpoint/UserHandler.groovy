package com.ohagner.deviations.api.user.endpoint

import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.repository.WatchRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.jackson.Jackson.json

@Slf4j
class UserHandler extends GroovyHandler {


    @Override
    void handle(GroovyContext ctx) throws Exception {
        ctx.with {
            //User is retrieved from DB from ApiToken
            User user = get(User)
            byMethod {
                delete {
                    //TODO: This should probably be moved to a service
                    UserRepository userRepository = context.get(UserRepository)
                    WatchRepository watchRepository = context.get(WatchRepository)
                    log.debug "Deleting user"
                    watchRepository.findByUsername(user.credentials.username).then { watches ->
                        watches.each { Watch watch ->
                            watchRepository.delete(user.credentials.username, watch.id).then { Optional<Watch> optWatch ->
                                log.info "Deleted watch: ${optWatch.present ?: optWatch.get().id } "
                            }
                        }
                    }

                    userRepository.delete(user)
                        .onError { t ->
                            log.info("Failed to delete user ${user.credentials.username}", t)
                            response.status(500)
                            render json([message: "Failed to delete user"])
                        }
                        .then { User deletedUser ->
                            render deletedUser
                        }

                }
                get {
                    render user
                }
                put {
                    log.debug "Updating user ${user.credentials.username}"
                    UserRepository userRepository = context.get(UserRepository)
                    request.getBody().then {
                        String request = it.text
                        log.debug "User update request: $request"
                        User update = User.fromJson(request)
                        user.firstName = update.firstName
                        user.lastName = update.lastName
                        user.emailAddress = update.emailAddress
                        userRepository.update(user.credentials.username, user)
                            .onError { throwable ->
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
}

package com.ohagner.deviations.api

import com.ohagner.deviations.api.deviation.endpoint.DeviationsChain
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.endpoint.CreateUserHandler
import com.ohagner.deviations.api.user.endpoint.UserAuthenticationHandler
import com.ohagner.deviations.api.user.endpoint.UserAuthorizationHandler
import com.ohagner.deviations.api.user.endpoint.UserHandler
import com.ohagner.deviations.api.user.repository.UserRepository
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.repository.WatchRepository
import com.ohagner.deviations.api.watch.router.WatchChain
import com.ohagner.deviations.api.watch.service.WatchProcessQueueingService
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class ApiChain extends GroovyChainAction {


    @Override
    void execute() throws Exception {
        post("authenticate", UserAuthenticationHandler)
        prefix("deviations", DeviationsChain)
        path("users") { UserRepository userRepository ->
            byMethod {
                //Remove this one later
                get {
                    userRepository.retrieveAll().then { users ->
                        render json(users)
                    }
                }
                post {
                    insert(new CreateUserHandler(userRepository))
                }
            }
        }
        prefix("users/:username") {
            all(UserAuthorizationHandler)
            path("") {
                insert(new UserHandler())
            }
            prefix("watches") {
                insert(new WatchChain())
            }
            post("deviationcheck") { WatchRepository watchRepository, WatchProcessQueueingService queueingService, User user ->
                log.debug "Retrieving watches for user ${user.credentials.username}"
                List<Watch> watchesToCheck = watchRepository.findByUsername(user.credentials.username)
                queueingService.enqueueForProcessing(watchesToCheck)
                String message = "Started deviation checking for ${watchesToCheck.size()} ${watchesToCheck.size() == 1 ? 'watch' : 'watches'}"
                render json(["message": message])
            }
        }
    }
}

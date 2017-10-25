package com.ohagner.deviations.api

import com.ohagner.deviations.api.deviation.endpoint.DeviationCheckHandler
import com.ohagner.deviations.api.deviation.endpoint.DeviationsChain
import com.ohagner.deviations.api.stop.endpoint.FindStopsHandler
import com.ohagner.deviations.api.stop.repository.StopRepository
import com.ohagner.deviations.api.user.endpoint.CreateUserHandler
import com.ohagner.deviations.api.user.endpoint.UserAuthenticationHandler
import com.ohagner.deviations.api.user.endpoint.UserAuthorizationHandler
import com.ohagner.deviations.api.user.endpoint.UserHandler
import com.ohagner.deviations.api.user.repository.UserRepository
import com.ohagner.deviations.api.watch.router.WatchChain
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
        get("stops") { StopRepository stopRepository ->
            insert(new FindStopsHandler(stopRepository))
        }
        prefix("users/:username") {
            all(UserAuthorizationHandler)
            path("") {
                insert(new UserHandler())
            }
            prefix("watches") {
                insert(new WatchChain())
            }
            post("deviationcheck", DeviationCheckHandler)
        }
    }
}

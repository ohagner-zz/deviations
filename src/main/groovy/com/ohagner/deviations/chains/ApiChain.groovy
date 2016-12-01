package com.ohagner.deviations.chains

import com.ohagner.deviations.handlers.CreateUserHandler
import com.ohagner.deviations.handlers.UserAuthenticationHandler
import com.ohagner.deviations.handlers.UserAuthorizationHandler
import com.ohagner.deviations.handlers.UserHandler
import com.ohagner.deviations.repository.UserRepository
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
                    render json(userRepository.retrieveAll())
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
            //A user should be able to trigger a watch check
            /*path("check") { WatchRepository watchRepository, UserRepository userRepository ->
                DeviationRepository deviationRepo = new HttpDeviationRepository()
                DeviationMatcher deviationMatcher = new DeviationMatcher(deviationRepo.retrieveAll())
                WatchProcessor processor = WatchProcessor.builder()
                        .deviationMatcher(deviationMatcher)
                        .deviationsApiClient(watchRepository).build()
                log.info "Before blocking"
                Blocking.exec { processor.process() }
                log.info "After blocking"
                render json(["message": "Checking user"])
            }*/
        }
    }
}

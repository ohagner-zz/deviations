package com.ohagner.deviations.handlers

import com.ohagner.deviations.domain.TransportMode
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.repository.DeviationRepository
import com.ohagner.deviations.repository.UserRepository
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.groovy.handling.GroovyChainAction
import ratpack.registry.Registry

import static ratpack.jackson.Jackson.json

@Slf4j
class ApiChain extends GroovyChainAction {


    @Override
    void execute() throws Exception {
        prefix("deviations") {
            get("") {
                DeviationRepository deviationRepo = context.get(DeviationRepository)
                render json(deviationRepo.retrieveAll())
            }
            get(":transportType") {
                DeviationRepository deviationRepo = context.get(DeviationRepository)
                Promise.sync {
                    TransportMode transportMode = TransportMode.valueOf(pathTokens.transportType)
                    deviationRepo.retrieveAll().findAll { it.transportMode == transportMode }
                }.onError { t ->
                    log.warn("Failed to retrieve deviations for transport", t)
                    response.status(500)
                    render json([message:"Failed to retrieve deviations"])
                }.then { deviationList ->
                    render json(deviationList)
                }
            }
            get(":transportType/:lineNumber") {
                DeviationRepository deviationRepo = context.get(DeviationRepository)
                Promise.sync {
                    TransportMode transportMode = TransportMode.valueOf(pathTokens.transportType)
                    String lineNumber = pathTokens.lineNumber
                    deviationRepo.retrieveAll().findAll { it.transportMode == transportMode && it.lineNumbers.contains(lineNumber) }
                }.onError { t ->
                    log.warn("Failed to retrieve deviations for transport and linenumber", t)
                    response.status(500)
                    render json([message:"Failed to retrieve deviations"])
                }.then { deviationList ->
                    render json(deviationList)
                }
            }
        }
        path("users") { UserRepository userRepository ->
            byMethod {
                get {
                    render json(userRepository.retrieveAll())
                }
                post {
                    request.getBody().then {
                        String request = it.text
                        User requestUser = User.fromJson(request)

                        if (userRepository.userExists(requestUser.username)) {
                            response.status(400)
                            render json(["message": "User already exists"])
                        } else {
                            //validate user
                            User createdUser = userRepository.create(requestUser)
                            response.status(201)
                            render createdUser.toJson()
                        }
                    }
                }
            }
        }
        prefix("users/:username") {
            all { UserRepository userRepository ->
                String username = pathTokens.username
                Optional<User> callingUser = userRepository.findByUsername(username)
                callingUser.isPresent() ? next(Registry.single(User, callingUser.get())) : next()
            }
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

package com.ohagner.deviations.chains

import com.ohagner.deviations.Role
import com.ohagner.deviations.domain.Deviation
import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.handlers.UserAuthorizationHandler
import com.ohagner.deviations.handlers.UserHandler
import com.ohagner.deviations.repository.DeviationRepository
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.security.AuthService
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class ApiChain extends GroovyChainAction {


    @Override
    void execute() throws Exception {
        post("authenticate") { AuthService authService ->
            request.body.then { body ->
                def jsonBody = new JsonSlurper().parseText(body.text)
                log.info "Authenticating with user ${jsonBody.username}"
                authService.authenticate(jsonBody.username, jsonBody.password).onNull {
                    response.status(401)
                    render json([message: "Invalid username and/or password"])
                }.then { User user ->
                    response.status(200)
                    render user
                }
            }
        }
        prefix("deviations") {
            get("") {
                DeviationRepository deviationRepo = context.get(DeviationRepository)
                render json(deviationRepo.retrieveAll())
            }
            get(":transportType") {
                DeviationRepository deviationRepo = context.get(DeviationRepository)
                Promise.sync {
                    Deviation.TransportMode transportMode = Deviation.TransportMode.valueOf(pathTokens.transportType)
                    deviationRepo.retrieveAll().findAll { it.transportMode == transportMode }
                }.onError { t ->
                    log.warn("Failed to retrieve deviations for transport", t)
                    response.status(500)
                    render json([message: "Failed to retrieve deviations"])
                }.then { deviationList ->
                    render json(deviationList)
                }
            }
            get(":transportType/:lineNumber") {
                DeviationRepository deviationRepo = context.get(DeviationRepository)
                Promise.sync {
                    Deviation.TransportMode transportMode = Deviation.TransportMode.valueOf(pathTokens.transportType)
                    String lineNumber = pathTokens.lineNumber
                    deviationRepo.retrieveAll().findAll {
                        it.transportMode == transportMode && it.lineNumbers.contains(lineNumber)
                    }
                }.onError { t ->
                    log.warn("Failed to retrieve deviations for transport and linenumber", t)
                    response.status(500)
                    render json([message: "Failed to retrieve deviations"])
                }.then { deviationList ->
                    render json(deviationList)
                }
            }
        }
        path("users") { UserRepository userRepository ->
            byMethod {
                //Remove this one later
                get {
                    render json(userRepository.retrieveAll())
                }
                post {
                    request.getBody().then {
                        String request = it.text
                        def json = new JsonSlurper().parseText(request)
                        String password = json.credentials.password
                        User requestUser = User.fromJson(request)

                        if (userRepository.userExists(requestUser.credentials.username)) {
                            response.status(400)
                            render json(["message": "User already exists"])
                        } else {
                            //validate user
                            requestUser.credentials.apiToken = null
                            requestUser.credentials.role = Role.USER
                            User createdUser = userRepository.create(requestUser, password)
                            response.status(201)
                            render createdUser
                        }
                    }
                }
            }
        }
        prefix("users/:username") {
            //Everything below here should have a security token header
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

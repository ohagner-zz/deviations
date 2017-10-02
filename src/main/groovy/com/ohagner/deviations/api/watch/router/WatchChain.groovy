package com.ohagner.deviations.api.watch.router

import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.endpoint.UpdateWatchHandler
import com.ohagner.deviations.api.watch.repository.WatchRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class WatchChain extends GroovyChainAction {

    @Override
    void execute() throws Exception {
        path("") { WatchRepository watchRepository, User user ->
            context.byMethod {
                post {
                    log.info("Creating watch here")
                    request.body.flatMap { body ->

                        Watch watch = Watch.fromJson(body.text)
                        watch.username = user.credentials.username
                        log.info "Creating watch for user ${user.credentials.username}"
                        watchRepository.create(watch)
                    }.onError { throwable ->
                        log.error("Failed to create watch", throwable)
                        response.status(500)
                        render json(["message":"Failed to create watch"])
                    }.then { Watch created ->
                        response.status(201)
                        render json(created)
                    }
                }
                get {
                    watchRepository.findByUsername(user.credentials.username)
                        .onError{ throwable ->
                            log.error("Failed to get all watches", e)
                            render json(["message": "Something went wrong"])
                        }.then {
                            render json(it)
                        }
                }
            }
        }
        path(":id") { WatchRepository watchRepository, User user ->
            if (!pathTokens.id.isNumber()) {
                response.status(400)
                render json(["message": new String("Watch id ${pathTokens.id} is not numeric")])
            }
            long watchId = pathTokens.asLong('id')
            context.byMethod {
                delete {
                    watchRepository.delete(user.credentials.username, watchId).then { Optional<Watch> deletedWatch ->
                        if (deletedWatch.isPresent()) {
                            render json(deletedWatch.get())
                        } else {
                            response.status(500)
                            render json(["message": new String("Watch with id $watchId could not be deleted")])
                        }
                    }

                }
                get {
                    watchRepository.findByUsernameAndId(user.credentials.username, watchId).then { Optional<Watch> watch ->
                        if (watch.isPresent()) {
                            render json(watch.get())
                        } else {
                            log.debug "Watch not found"
                            response.status(404)
                            render json(["message": new String("Watch with id $watchId does not exist")])
                        }
                    }
                }
                put {
                    insert(new UpdateWatchHandler(watchRepository))
                }
            }
        }
    }
}

package com.ohagner.deviations.api.watch

import com.ohagner.deviations.api.user.User
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
                    request.body.map { body ->
                        log.debug "Creating watch for user ${user.credentials.username}"
                        Watch watch = Watch.fromJson(body.text)
                        watch.username = user.credentials.username
                        response.status(201)
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
                    render json(watchRepository.findByUsername(user.credentials.username))
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
                    Optional<Watch> deletedWatch = watchRepository.delete(user.credentials.username, watchId)
                    if (deletedWatch.isPresent()) {
                        render json(deletedWatch.get())
                    } else {
                        response.status(500)
                        render json(["message": new String("Watch with id $watchId could not be deleted")])
                    }
                }
                get {
                    Optional<Watch> watch = watchRepository.findByUsernameAndId(user.credentials.username, watchId)
                    if (watch.isPresent()) {
                        render json(watch.get())
                    } else {
                        log.debug "Watch not found"
                        response.status(404)
                        render json(["message": new String("Watch with id $watchId does not exist")])
                    }
                }
                put {
                    insert(new UpdateWatchHandler(watchRepository))
                }
            }
        }
    }
}

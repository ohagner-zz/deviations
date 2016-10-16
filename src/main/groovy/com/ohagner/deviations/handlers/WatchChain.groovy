package com.ohagner.deviations.handlers

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.repository.WatchRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class WatchChain extends GroovyChainAction {

    @Override
    void execute() throws Exception {
        all {
            Optional<User> user = context.maybeGet(User)
            if (!user.isPresent()) {
                log.debug "User not found"
                response.status(404)
                render json(["message": "User not found"])
            } else {
                next()
            }
        }
        path("") { WatchRepository watchRepository, User user ->
            context.byMethod {
                post {
                    request.body.then { body ->
                        log.debug "Creating watch for user ${user.username}"
                        Watch watch = Watch.fromJson(body.text)
                        watch.username = user.username
                        response.status(201)
                        render json(watchRepository.create(watch))
                    }
                }
                get {
                    render json(watchRepository.findByUsername(user.username))
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
                    Optional<Watch> deletedWatch = watchRepository.delete(user.username, watchId)
                    if (deletedWatch.isPresent()) {
                        render json(deletedWatch.get())
                    } else {
                        response.status(500)
                        render json(["message": new String("Watch with id $watchId could not be deleted")])
                    }
                }
                get {
                    Optional<Watch> watch = watchRepository.findByUsernameAndId(user.username, watchId)
                    if (watch.isPresent()) {
                        render json(watch.get())
                    } else {
                        log.debug "Watch not found"
                        response.status(404)
                        render json(["message": "Watch with id $watchId does not exist"])
                    }
                }
                put {
                    request.body.map { body ->
                        Watch watchToUpdate = Watch.fromJson(body.text)
                        assert watchToUpdate.id == watchId
                        watchRepository.update(watchToUpdate)
                    }.onError { t ->
                        log.error("Failed to update watch", t)
                    }.then {
                        response.status(200)
                        render json([message: "Successfully updated watch"])
                    }

                }
            }
        }
    }
}

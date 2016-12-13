package com.ohagner.deviations.api.watch.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.repository.WatchRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.jackson.Jackson.json

@Slf4j
class UpdateWatchHandler extends GroovyHandler {

    WatchRepository watchRepository

    @Inject
    UpdateWatchHandler(WatchRepository watchRepository) {
        this.watchRepository = watchRepository
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            long watchId = pathTokens.asLong('id')
            request.body.map { body ->
                Watch watchToUpdate = Watch.fromJson(body.text)
                assert watchToUpdate.id == watchId
                watchRepository.update(watchToUpdate)
            }.onError { t ->
                log.error("Failed to update watch", t)
                response.status(500)
                render json([message: "Failed to update watch"])
            }.then { watch ->
                response.status(200)
                render json(watch)
            }
        }

    }
}

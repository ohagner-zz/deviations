package com.ohagner.deviations.api.stop.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.stop.domain.Stop
import com.ohagner.deviations.api.stop.repository.StopRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.jackson.Jackson.json

@Slf4j
class FindStopsHandler extends GroovyHandler {

    StopRepository stopRepository

    @Inject
    FindStopsHandler(StopRepository stopRepository) {
        this.stopRepository = stopRepository
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            stopRepository.findStops(request.queryParams.name)
                .onError() { Throwable t ->
                    log.error("Failed to retrieve stops", t)
                    response.status(500)
                    render json([message: "Failed to retrieve stops"])
                }.then { List<Stop> stops ->
                    render json(stops)
                }
        }
    }
}


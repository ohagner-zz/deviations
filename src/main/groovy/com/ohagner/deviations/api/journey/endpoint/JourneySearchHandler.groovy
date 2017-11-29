package com.ohagner.deviations.api.journey.endpoint

import com.ohagner.deviations.api.journey.domain.JourneySearch
import com.ohagner.deviations.api.journey.repository.JourneyRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.jackson.Jackson

@Slf4j
class JourneySearchHandler extends GroovyHandler {

    JourneyRepository journeyRepository

    JourneySearchHandler(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            parse(JourneySearch.class).flatMap { journeySearch ->
                journeyRepository.search(journeySearch)
            }.onError { Throwable t ->
                log.error("Failed to search for journey", t)
                response.status 500
                render Jackson.json([message: "Journey search failed"])
            }.then { journeys ->
                render Jackson.json(journeys)
            }
        }
    }
}

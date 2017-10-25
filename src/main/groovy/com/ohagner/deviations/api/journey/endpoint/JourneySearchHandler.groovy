package com.ohagner.deviations.api.journey.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.journey.domain.Journey
import com.ohagner.deviations.api.journey.domain.JourneySearch
import com.ohagner.deviations.api.journey.repository.JourneyRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.jackson.Jackson

@Slf4j
class JourneySearchHandler extends GroovyHandler {

    JourneyRepository journeyRepository

    @Inject
    JourneySearchHandler(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            //parse(JourneySearch.class).flatMap { journeySearch ->
            request.body.flatMap { body ->
                journeyRepository.search(new JourneySearch())
            }.then { journeys ->
                render Jackson.json(journeys)
            }
        }
    }
}

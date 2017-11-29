package com.ohagner.deviations.api.journey.router

import com.google.inject.Inject
import com.ohagner.deviations.api.journey.endpoint.JourneySearchHandler
import com.ohagner.deviations.api.journey.repository.JourneyRepository
import ratpack.groovy.handling.GroovyChainAction


class JourneyChain extends GroovyChainAction {

    JourneyRepository journeyRepository

    @Inject
    JourneyChain(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository
    }

    @Override
    void execute() throws Exception {

        post("journeysearch") {
            insert new JourneySearchHandler(journeyRepository)
        }

    }
}

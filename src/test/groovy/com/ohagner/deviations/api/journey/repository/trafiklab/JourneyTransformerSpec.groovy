package com.ohagner.deviations.api.journey.repository.trafiklab

import com.ohagner.deviations.api.journey.domain.Journey
import spock.lang.Specification


class JourneyTransformerSpec extends Specification {

    JourneyTransformer transformer = new JourneyTransformer()

    void 'should transform response with multiple journeys'() {
        given:
            String resePlanerareResponse = new File('src/test/resources/trafiklab/reseplanerare/reseplanerareResponse.json').text
        when:
            List<Journey> journeys = transformer.fromReseplanerareResponse(resePlanerareResponse)
        then:
            journeys.size() == 5
    }
}
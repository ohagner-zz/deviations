package com.ohagner.deviations.api.journey.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.ohagner.deviations.api.journey.domain.Journey
import com.ohagner.deviations.api.journey.domain.Leg
import com.ohagner.deviations.api.journey.repository.JourneyRepository
import com.ohagner.deviations.api.stop.domain.Stop
import groovy.json.JsonOutput
import ratpack.exec.Promise
import ratpack.groovy.test.handling.GroovyRequestFixture
import ratpack.jackson.JsonRender
import ratpack.jackson.internal.DefaultJsonRender
import ratpack.test.handling.HandlingResult
import spock.lang.Specification

import java.time.LocalDateTime

class JourneySearchHandlerSpec extends Specification {

    JourneyRepository journeyRepository = Mock()
    JourneySearchHandler handler = new JourneySearchHandler(journeyRepository)

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()

    void 'should return list of journeys'() {
        given:
            Journey repositoryResponse = createDefaultJourney()
            journeyRepository.search(_) >> Promise.value([repositoryResponse])
            String request = JsonOutput.toJson {
                from "2016-12-24'T'12:13:14"
                to "2016-12-24'T'13:15:15"
                origin {
                    name "Origin"
                    externalId "12345"
                }
                destination {
                    name "Destination"
                    externalId "23456"
                }
            }
            GroovyRequestFixture requestFixture = GroovyRequestFixture.requestFixture().body(request, "application/json")
        when:
            HandlingResult result = requestFixture.handle(handler)
        then:
            result.status.'2xx'
            result.rendered(JsonRender).object == [repositoryResponse]
    }

    Journey createDefaultJourney() {
        Journey journey = new Journey()
        journey.departure = LocalDateTime.now()
        journey.arrival = LocalDateTime.now().plusHours(1)
        journey.origin = new Stop(externalId: "1234", name: "Origin")
        journey.destination = new Stop(externalId: "5678", name: "Destination")

        Leg leg = new Leg(arrival: LocalDateTime.now().plusHours(1), departure: LocalDateTime.now(), origin: new Stop(externalId: "1234", name: "Origin"), destination: new Stop(externalId: "5678", name: "Destination"))
        journey.legs.add(leg)
        return journey
    }

}

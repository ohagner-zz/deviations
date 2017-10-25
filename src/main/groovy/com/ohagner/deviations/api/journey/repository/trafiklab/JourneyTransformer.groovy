package com.ohagner.deviations.api.journey.repository.trafiklab

import com.ohagner.deviations.api.journey.domain.Journey
import com.ohagner.deviations.api.journey.domain.Leg
import com.ohagner.deviations.api.stop.domain.Stop
import com.ohagner.deviations.api.transport.domain.Transport
import com.ohagner.deviations.api.transport.domain.TransportMode
import groovy.json.JsonSlurper
import java.time.*
import java.time.format.DateTimeFormatter

class JourneyTransformer {

    List<Journey> fromReseplanerareResponse(String resePlanerareResponse) {
        def jsonResponse = new JsonSlurper().parseText(resePlanerareResponse)

        def journeys = []
        jsonResponse.Trip.each { trip ->
            Journey journey = new Journey()
            journey.origin = new Stop(name: "Segersäng")
            journey.destination = new Stop(name: "Spånga")
            trip.LegList.Leg.each { leg ->
                Leg journeyLeg = new Leg(origin: new Stop(name: leg.Origin.name), destination: new Stop(name: leg.Destination.name))
                journeyLeg.transport = new Transport(transportMode: TransportMode.valueOf(leg.Product.catOut.trim()), line: leg.Product.line)
                journeyLeg.departure = LocalDateTime.parse("${leg.Origin.date}T${leg.Origin.time}", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                journeyLeg.arrival = LocalDateTime.parse("${leg.Destination.date}T${leg.Destination.time}", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                journey.legs.add(journeyLeg)
            }
            journey.departure = journey.legs[0].departure
            journey.arrival = journey.legs[-1].arrival
            journeys.add(journey)
        }

        return journeys
    }

}

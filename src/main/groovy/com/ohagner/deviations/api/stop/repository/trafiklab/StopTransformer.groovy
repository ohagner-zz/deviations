package com.ohagner.deviations.api.stop.repository.trafiklab

import com.ohagner.deviations.api.stop.domain.Stop
import groovy.json.JsonSlurper

class StopTransformer {

    List<Stop> fromStopResponse(String stopResponseJsonString) {
        def response = new JsonSlurper().parseText(stopResponseJsonString)
        List<Stop> stopList = []
        response.ResponseData.each { externalStop ->
            stopList.add(new Stop(externalId: externalStop.SiteId, name: externalStop.Name))
        }
        return stopList
    }

}

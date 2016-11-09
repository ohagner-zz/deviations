package com.ohagner.deviations.repository

import com.ohagner.deviations.DeviationFilter
import com.ohagner.deviations.domain.Deviation

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.RESTClientException

@Slf4j
class HttpDeviationRepository implements DeviationRepository {

    RESTClient trafikLabClient
    String apiKey

    public HttpDeviationRepository(RESTClient trafikLabClient, String apikey) {
        this.trafikLabClient = trafikLabClient
        this.apiKey = apikey
    }


    List<Deviation> retrieveAll() {

        log.info "Updating deviations through http call"
        List<Deviation> deviationList = []
        try {

            def response = trafikLabClient.get(query: [key: apiKey, transportMode: Deviation.TransportMode.TRAIN.toString()], accept: ContentType.JSON)
            log.debug "Received ${response.json}"

            deviationList.addAll(retrieveDeviationsForTransport(Deviation.TransportMode.TRAIN))
            deviationList.addAll(retrieveDeviationsForTransport(Deviation.TransportMode.BUS))
            deviationList.addAll(retrieveDeviationsForTransport(Deviation.TransportMode.SUBWAY))

            log.debug "Retrieved ${deviationList.size()} deviations"
        } catch (RESTClientException exception) {
            log.error("Failed to retrieve deviations", exception)
            deviationList = []
            //TODO: Send some sort of notification
        }

        return DeviationFilter.apply(deviationList).asImmutable()
    }

    private List<Deviation> retrieveDeviationsForTransport(Deviation.TransportMode transportMode) {
        def response = trafikLabClient.get(query: [key: apiKey, transportMode: transportMode.toString()], accept: ContentType.JSON)
        log.debug "Received ${response.json}"

        def jsonDeviations = response.json
        return jsonDeviations.ResponseData.collect {
            Deviation.fromJson(JsonOutput.toJson(it), transportMode)
        }
    }
}


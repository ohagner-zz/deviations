package com.ohagner.deviations.repository

import com.ohagner.deviations.domain.Deviation
import com.ohagner.deviations.domain.TransportMode
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
        this.apiKey = apiKey
    }


    List<Deviation> retrieveAll() {

        log.info "Updating deviations"
        List<Deviation> deviationList = []
        try {

            def response = trafikLabClient.get(query: [key: apiKey, transportMode: TransportMode.TRAIN.toString()], accept: ContentType.JSON)
            log.debug "Received ${response.json}"

            deviationList.addAll(retrieveDeviationsForTransport(TransportMode.TRAIN))
            deviationList.addAll(retrieveDeviationsForTransport(TransportMode.BUS))
            deviationList.addAll(retrieveDeviationsForTransport(TransportMode.SUBWAY))

            log.debug "Retrieved ${deviationList.size()} deviations"
        } catch (RESTClientException exception) {
            log.error("Failed to retrieve deviations", exception)
            deviationList = []
            //TODO: Send some sort of notification
        }

        return deviationList.asImmutable()
    }

    private List<Deviation> retrieveDeviationsForTransport(TransportMode transportMode) {
        def response = trafikLabClient.get(query: [key: apiKey, transportMode: transportMode.toString()], accept: ContentType.JSON)
        log.info "Received ${response.json}"

        def jsonDeviations = response.json
        return jsonDeviations.ResponseData.collect {
            log.info "Doing stuff"
            Deviation.fromJson(JsonOutput.toJson(it), TransportMode.TRAIN)
        }
    }
}


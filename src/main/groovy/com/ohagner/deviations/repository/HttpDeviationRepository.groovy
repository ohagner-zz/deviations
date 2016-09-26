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
//    public static final String URL = "http://localhost:4549/api2/deviations.json"

//    public static final String URL = "http://api.sl.se/api2/deviations.json"
//    public static final String API_KEY = "0e21305879774373b47a8fd8262792aa"

    RESTClient trafikLabClient
    String apiKey

    public HttpDeviationRepository(RESTClient trafikLabClient, String apikey) {
        this.trafikLabClient = trafikLabClient
        this.apiKey = apiKey
        deviationList = []
    }

    private List<Deviation> deviationList

    List<Deviation> retrieveAll() {

        log.info "Updating deviations"

        try {
            deviationList.clear()

            def response = trafikLabClient.get(query: [key: apiKey, transportMode: TransportMode.TRAIN.toString()], accept: ContentType.JSON)
            log.debug "Received ${response.json}"

            def jsonDeviations = response.json
            jsonDeviations.ResponseData.each {
                deviationList.add(Deviation.fromJson(JsonOutput.toJson(it), TransportMode.TRAIN,))
            }

            log.info "Current deviations:"
            deviationList.each {
                log.info "Lines: ${it.lineNumbers}, Header: ${it.header}"
            }
        } catch(RESTClientException exception) {
            log.error("Failed to retrieve deviations")
            deviationList = []
        }

        return deviationList.asImmutable()
    }

    List<Deviation> getFiltered(Closure filter) {
        return retrieveAll().findAll { filter(it) }
    }

//    def parseJson(String json) {
//        new JsonSlurper().parseText(json)
//    }
//
//    def parseJson(Object json) {
//        new JsonSlurper().parse(json)
//    }

}

package com.ohagner.deviations

import com.ohagner.deviations.domain.TransportMode
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.RESTClientException

@Slf4j
class HttpDeviationRepo implements DeviationRepo {
    public static final String URL = "http://localhost:4549/api2/deviations.json"

//    public static final String URL = "http://api.sl.se/api2/deviations.json"
    public static final String API_KEY = "0e21305879774373b47a8fd8262792aa"

    RESTClient trafikLabClient

    public HttpDeviationRepo(RESTClient trafikLabClient) {
        this.trafikLabClient = trafikLabClient
        deviationList = []
    }

    public HttpDeviationRepo() {
        trafikLabClient = new RESTClient(URL)
        deviationList = []
    }

    private List<Deviation> deviationList

    List<Deviation> retrieveAll() {

        log.info "Updating deviations"

        try {
            //Sätt fromdate till 24h bakåt to-date 24h framåt
            def response = trafikLabClient.get(query: [key: API_KEY, transportMode: "TRAIN"], accept: ContentType.JSON)
            log.debug "Received ${response.json}"
            deviationList.clear()

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

    def parseJson(String json) {
        new JsonSlurper().parseText(json)
    }

    def parseJson(Object json) {
        new JsonSlurper().parse(json)
    }

}

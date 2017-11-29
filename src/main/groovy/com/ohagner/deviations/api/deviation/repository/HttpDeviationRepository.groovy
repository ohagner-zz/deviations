package com.ohagner.deviations.api.deviation.repository

import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.transport.domain.TransportMode
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.RESTClientException
import wslite.rest.Response

@Slf4j
class HttpDeviationRepository implements DeviationRepository {

    RESTClient trafikLabClient
    String apiKey

    public HttpDeviationRepository(RESTClient trafikLabClient, String apikey) {
        this.trafikLabClient = trafikLabClient
        this.apiKey = apikey
    }


    Promise<List<Deviation>> retrieveAll() {

        Promise.sync {
            log.info "Updating deviations through http call"
            List<Deviation> deviationList = []
            try {

                deviationList.addAll(retrieveDeviationsForTransport(TransportMode.TRAIN))
                deviationList.addAll(retrieveDeviationsForTransport(TransportMode.BUS))
                deviationList.addAll(retrieveDeviationsForTransport(TransportMode.METRO))


                log.debug "Retrieved ${deviationList.size()} deviations"
            } catch (RESTClientException exception) {
                log.error("Failed to retrieve deviations calling URL ${trafikLabClient.getUrl()}", exception)
                deviationList = []
                //TODO: Send some sort of notification
            }

            return DeviationFilter.apply(deviationList).asImmutable()
        }
    }

    List<Deviation> retrieveDeviationsForTransport(TransportMode transportMode) {
        Response response = trafikLabClient.get(query: [key: apiKey, transportMode: transportMode.toString()], accept: ContentType.JSON)
        log.debug "Received ${response.json} and response code ${response.statusCode}"

        def jsonDeviations = response.json
        return jsonDeviations.ResponseData.collect {
            Deviation.fromJson(JsonOutput.toJson(it), transportMode)
        }
    }
}


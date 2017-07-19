package com.ohagner.deviations.api.deviation.repository

import com.ohagner.deviations.api.deviation.domain.Deviation
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

                deviationList.addAll(retrieveDeviationsForTransport(Deviation.TransportMode.TRAIN))
                deviationList.addAll(retrieveDeviationsForTransport(Deviation.TransportMode.BUS))
                deviationList.addAll(retrieveDeviationsForTransport(Deviation.TransportMode.SUBWAY))

                log.info "Retrieved ${deviationList.size()} deviations"
            } catch (RESTClientException exception) {
                log.error("Failed to retrieve deviations", exception)
                deviationList = []
                //TODO: Send some sort of notification
            } catch(Exception e) {
                log.error("ERROR", e)
            }

            return DeviationFilter.apply(deviationList).asImmutable()
        }
    }

    List<Deviation> retrieveDeviationsForTransport(Deviation.TransportMode transportMode) {
        Response response = trafikLabClient.get(query: [key: apiKey, transportMode: transportMode.toString()], accept: ContentType.JSON)
        log.debug "Received ${response.json} and response code ${response.statusCode}"

        def jsonDeviations = response.json
        return jsonDeviations.ResponseData.collect {
            Deviation.fromJson(JsonOutput.toJson(it), transportMode)
        }
    }
}


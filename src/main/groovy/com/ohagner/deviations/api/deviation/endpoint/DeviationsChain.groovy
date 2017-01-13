package com.ohagner.deviations.api.deviation.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.deviation.repository.DeviationRepository
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class DeviationsChain extends GroovyChainAction {

    DeviationRepository deviationRepository

    @Inject
    DeviationsChain(DeviationRepository deviationRepository) {
        this.deviationRepository = deviationRepository
    }

    @Override
    void execute() throws Exception {
        get("") {
            Promise.sync {
                retrieveDeviations()
            }.onError { t ->
                log.warn("Failed to retrieve deviations", t)
                response.status(500)
                render json([message: "Failed to retrieve deviations"])
            }.then { deviationList ->
                render json(deviationList)
            }
        }
        get(":transportType") {
            Promise.sync {
                Deviation.TransportMode transportMode = Deviation.TransportMode.valueOf(pathTokens.transportType)
                retrieveDeviations() { it.transportMode == transportMode }
            }.onError { t ->
                log.warn("Failed to retrieve deviations for transport", t)
                response.status(500)
                render json([message: "Failed to retrieve deviations"])
            }.then { deviationList ->
                render json(deviationList)
            }
        }
        get(":transportType/:lineNumber") {
            Promise.sync {
                Deviation.TransportMode transportMode = Deviation.TransportMode.valueOf(pathTokens.transportType)
                String lineNumber = pathTokens.lineNumber
                retrieveDeviations() {
                    it.transportMode == transportMode && it.lineNumbers.contains(lineNumber)
                }
            }.onError { t ->
                log.warn("Failed to retrieve deviations for transport and linenumber", t)
                response.status(500)
                render json([message: "Failed to retrieve deviations"])
            }.then { deviationList ->
                render json(deviationList)
            }
        }
    }

    List<Deviation> retrieveDeviations(Closure filter = {true}) {
        deviationRepository.retrieveAll().findAll(filter)
    }
}

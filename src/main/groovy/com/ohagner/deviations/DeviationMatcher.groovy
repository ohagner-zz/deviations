package com.ohagner.deviations

import com.ohagner.deviations.domain.Transport
import com.ohagner.deviations.domain.Watch
import groovy.util.logging.Slf4j

@Slf4j
class DeviationMatcher {
    //Make immutable?
    final Map<Transport, List<Deviation>> transportDeviationMap

    public DeviationMatcher(List<Deviation> deviationList) {
        log.info "Initializing DeviationMatcher with ${deviationList?.size()} deviations"
        transportDeviationMap = new HashMap<>()
        deviationList.each { deviation ->
            deviation.lineNumbers.each { lineNumber ->
                log.info "Adding linenumber $lineNumber"
                def transport = new Transport(transportMode: deviation.transportMode, line: lineNumber)
                transportDeviationMap.get(transport, []).add(deviation)
            }
        }
        transportDeviationMap.each { key, value -> log.info "Adding transport/deviation key: ${key.toString()}, value: $value"}
    }

    Set<Deviation> findMatching(Watch watch) {
        Set<Deviation> matchingDeviations = []
        watch.transports.each {
            matchingDeviations.addAll(transportDeviationMap.get(it, []))
        }
        log.info "Matching watch: ${watch.name}. Found ${matchingDeviations.size()} match(es)."
        return matchingDeviations
    }

}

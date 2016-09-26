package com.ohagner.deviations

import com.ohagner.deviations.domain.Deviation
import com.ohagner.deviations.domain.Transport
import com.ohagner.deviations.domain.Watch
import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime

@Slf4j
class DeviationMatcher {
    //Make immutable?
    public static final int MAX_DEVIATION_DURATION_HOURS = 12
    public static final int MAX_DEVIATION_CREATED_HOURS_AGO = 12
    final Map<Transport, List<Deviation>> transportDeviationMap

    public DeviationMatcher(List<Deviation> deviationList) {
        log.info "Initializing DeviationMatcher with ${deviationList?.size()} deviations"
        transportDeviationMap = new HashMap<>()
        deviationList.findAll { Deviation deviation ->
            deviation.getDuration().toHours() < MAX_DEVIATION_DURATION_HOURS && Duration.between(LocalDateTime.now(), deviation.created).toHours() < MAX_DEVIATION_CREATED_HOURS_AGO
        }
        .each { deviation ->
            deviation.lineNumbers.each { lineNumber ->
                def transport = new Transport(transportMode: deviation.transportMode, line: lineNumber)
                log.debug "Adding deviation to matcher with linenumber $lineNumber"
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
        return matchingDeviations.findAll { watch.processedDeviationIds.contains(it.id) == false }
    }


}

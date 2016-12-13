package com.ohagner.deviations.worker.watch.service

import com.google.common.base.Stopwatch
import com.ohagner.deviations.api.deviation.service.DeviationMatcher
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.worker.deviation.service.DeviationsApiClient
import com.ohagner.deviations.worker.watch.domain.WatchProcessingResult
import com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder
import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import static com.ohagner.deviations.config.Constants.ZONE_ID

/**
 * Process watches and notify if one or more deviations are found
 */
@Slf4j
@TupleConstructor
@Builder
class WatchProcessor {

    DeviationMatcher deviationMatcher
    DeviationsApiClient deviationsApiClient


    WatchProcessingResult process(Watch watch) {
        WatchProcessingResult result = new WatchProcessingResult(status: WatchProcessingStatus.STARTED)
        Stopwatch timer = Stopwatch.createStarted()
        try {
            if (watch.isTimeToCheck(LocalDateTime.now(ZONE_ID))) {
                result.matchingDeviations = deviationMatcher.findMatching(watch)

                if(result.matchingDeviations) {
                    result.addMessage("Matching deviations: ${result.matchingDeviations.collect { it.id}.join(",") }")
                    result.status = WatchProcessingStatus.MATCHED
                    if(!deviationsApiClient.sendNotifications(watch, result.matchingDeviations)) {
                        result.status = WatchProcessingStatus.NOTIFICATION_FAILED
                        result.executionTime = Duration.ofMillis(timer.elapsed(TimeUnit.MILLISECONDS))
                        return result
                    }
                    result.status = WatchProcessingStatus.NOTIFIED
                    watch.lastNotified = LocalDateTime.now(ZONE_ID)
                    result.matchingDeviations.each { watch.addDeviationId(it.id)}

                } else {
                    result.status = WatchProcessingStatus.NO_MATCH
                    result.addMessage("No matching deviations found")
                }
            } else {
                result.status = WatchProcessingStatus.NOT_TIME_TO_CHECK
            }
            watch.lastProcessed = LocalDateTime.now(ZONE_ID)
            if(!deviationsApiClient.update(watch)) {
                result.status = WatchProcessingStatus.WATCH_UPDATE_FAILED
            }
        } catch(Exception e) {
            log.error("Watch processing failed for watch ${watch.id}", e)
            result.status = WatchProcessingStatus.FAILED
            result.addMessage(e.getMessage())
        }
        result.executionTime = Duration.ofMillis(timer.elapsed(TimeUnit.MILLISECONDS))
        return result
    }

}

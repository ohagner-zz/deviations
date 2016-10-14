package com.ohagner.deviations.watch

import com.google.common.base.Stopwatch
import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.domain.Deviation
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.domain.notifications.Notification
import com.ohagner.deviations.watch.task.WatchProcessingStatus
import com.ohagner.deviations.watch.task.WatchProcessingResult
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder
import groovy.util.logging.Slf4j
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.Response

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
    RESTClient deviationsApiClient


    WatchProcessingResult process(Watch watch) {
        WatchProcessingResult result = new WatchProcessingResult(status: WatchProcessingStatus.STARTED)
        Stopwatch timer = Stopwatch.createStarted()
        try {
            if (watch.isTimeToCheck(LocalDateTime.now(ZONE_ID))) {
                result.matchingDeviations = deviationMatcher.findMatching(watch)

                if(result.matchingDeviations) {
                    result.addMessage("Matching deviations: ${result.matchingDeviations.collect { it.id}.join(",") }")
                    result.status = WatchProcessingStatus.MATCHED
                    if(!sendNotifications(watch, result.matchingDeviations)) {
                        result.status = WatchProcessingStatus.NOTIFICATION_FAILED
                        result.executionTime = Duration.ofMillis(timer.elapsed(TimeUnit.MILLISECONDS))
                        return result
                    }
                    result.status = WatchProcessingStatus.NOTIFIED
                    watch.lastNotified = LocalDateTime.now(ZONE_ID)
                    result.matchingDeviations.each { watch.addDeviationId(it.id)}

                    if(!update(watch)) {
                        result.status = WatchProcessingStatus.WATCH_UPDATE_FAILED
                    }
                } else {
                    result.status = WatchProcessingStatus.NO_MATCH
                    result.addMessage("No matching deviations found")
                }
            } else {
                result.status = WatchProcessingStatus.NOT_TIME_TO_CHECK
            }
        } catch(Exception e) {
            log.error("Watch processing failed for watch ${watch.id}", e)
            result.status = WatchProcessingStatus.FAILED
            result.addMessage(e.getMessage())
        }
        result.executionTime = Duration.ofMillis(timer.elapsed(TimeUnit.MILLISECONDS))
        return result
    }

    private boolean sendNotifications(Watch watch, Set<Deviation> matchingDeviations) {
        try {
            Notification notification = Notification.fromDeviations(matchingDeviations, watch.notifyBy)
            log.info "Sending notification ${notification.toString()}"
            Response notificationResponse = deviationsApiClient.post(path: "/admin/users/${watch.username}/notification") {
                type ContentType.JSON
                text notification.toJson()
            }
            log.info "Notification response status ${notificationResponse.statusCode}"
            return notificationResponse.statusCode ==~  /2\d\d/
        } catch(Exception e) {
            log.error("Failed to send notification", e)
            return false
        }

    }

    private boolean update(Watch watch) {
        try {
            Response updateResponse = deviationsApiClient.put(path: "/users/${watch.username}/watches/${watch.id}") {
                type ContentType.JSON
                text watch.toJson()
            }
            log.debug "Update response status ${updateResponse.statusCode}"
            return updateResponse.statusCode ==~  /2\d\d/
        } catch(Exception e) {
            log.error("Failed to send notification", e)
            return false
        }

    }


}

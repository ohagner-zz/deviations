package com.ohagner.deviations.watch.task

import com.google.common.base.Stopwatch
import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.WatchRepository
import groovy.transform.builder.Builder
import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

@Slf4j
@Builder
class WatchTask implements Callable<WatchResult> {

    Watch watch

    DeviationMatcher deviationMatcher

    NotificationService notificationService

    WatchRepository watchRepository

    @Override
    WatchResult call() throws Exception {
        WatchResult result = new WatchResult(status: WatchExecutionStatus.STARTED)
        Stopwatch timer = Stopwatch.createStarted()
        try {
            if (watch.isTimeToCheck(LocalDateTime.now())) {
                def matchingDeviations = deviationMatcher.findMatching(watch)
                result.status = WatchExecutionStatus.TIME_CHECKED

                if(matchingDeviations) {
                    result.addMessage("Deviations matched: ${matchingDeviations.size()}")
                    result.status = WatchExecutionStatus.MATCHED
                    notificationService.processNotifications(watch, matchingDeviations)
                    result.status = WatchExecutionStatus.NOTIFIED
                    watch.lastNotified = LocalDateTime.now(ZoneId.of("Europe/Paris"))
                    watch.processedDeviationIds.addAll(matchingDeviations.collect { it.id })
                    watchRepository.update(watch)
                } else {
                    result.status = WatchExecutionStatus.NO_MATCH
                    result.addMessage("No matching deviations found")
                }
            } else {
                result.status = WatchExecutionStatus.NOT_TIME_TO_CHECK
            }
        } catch(Exception e) {
            log.error("Task execution failed", e)
            result.status = WatchExecutionStatus.FAILED
            result.addMessage(e.getMessage())
        }
        result.executionTime = Duration.ofMillis(timer.elapsed(TimeUnit.MILLISECONDS))
        return result
    }
}

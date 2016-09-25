package com.ohagner.deviations.task

import com.google.common.base.Stopwatch
import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.notifications.NotificationService
import groovy.util.logging.Slf4j

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

@Slf4j
class WatchTask implements Callable<WatchResult> {

    Watch watch

    DeviationMatcher deviationMatcher

    NotificationService notificationService

    @Override
    WatchResult call() throws Exception {
        WatchResult result = new WatchResult(status: WatchExecutionStatus.STARTED)
        Stopwatch timer = Stopwatch.createStarted()
        try {

            if (watch.isTimeToCheck(LocalDateTime.now())) {
                def deviations = deviationMatcher.findMatching(watch)
                result.status = WatchExecutionStatus.TIME_CHECKED

                if(deviations) {
                    result.status = WatchExecutionStatus.MATCHED
                    result.addMessage("Deviations matched: ${deviations.size()}")
                    notificationService.processNotifications(watch, deviations)
                    result.status = WatchExecutionStatus.NOTIFIED
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

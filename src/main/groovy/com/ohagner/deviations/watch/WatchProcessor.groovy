package com.ohagner.deviations.watch

import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
import com.ohagner.deviations.watch.task.WatchExecutionStatus
import com.ohagner.deviations.watch.task.WatchResult
import com.ohagner.deviations.watch.task.WatchTask
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder
import groovy.util.logging.Slf4j

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Process watches and notify if one or more deviations are found
 */
@Slf4j
@TupleConstructor
@Builder
class WatchProcessor {

    DeviationMatcher deviationMatcher
    NotificationService notificationService
    WatchRepository watchRepository

    Map<WatchExecutionStatus, List<WatchResult>> process() {

        List<Future<WatchResult>> watchExecutionResults = submitForProcessing(watchRepository.retrieveNextToProcess(50))

        return handleResults(watchExecutionResults)
    }

    private List<Future<WatchResult>> submitForProcessing(List<Watch> watches) {
        def watchTasks = watches.collect { watch -> new WatchTask(watch: watch, watchRepository: watchRepository, deviationMatcher: deviationMatcher, notificationService: notificationService) }
        log.info "Submitting ${watchTasks.size()} tasks to executor"
        ExecutorService executorService = Executors.newFixedThreadPool(2)
        return executorService.invokeAll(watchTasks, 5, TimeUnit.MINUTES)
    }

    private Map<WatchExecutionStatus, List<WatchResult>> handleResults(List<Future<WatchResult>> watchExecutionResults) {
        Map<WatchExecutionStatus, List<WatchResult>> resultsByStatus = [:]
        watchExecutionResults.each { future ->
            //Probably a try-catch somewhere around here
            WatchResult result = future.get(5, TimeUnit.SECONDS)
            resultsByStatus.get(result.status, []) << result
        }

        List executionSummary = []
        WatchExecutionStatus.values().each {
            executionSummary.add("Status $it: with " + resultsByStatus.get(it, []).size() + " watches")
        }
        log.info executionSummary.join("\n")
        return resultsByStatus
    }

}

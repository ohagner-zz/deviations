package com.ohagner.deviations.api.watch.service

import com.google.inject.Inject
import com.ohagner.deviations.api.watch.repository.WatchRepository
import com.ohagner.deviations.config.AppConfig
import com.ohagner.deviations.config.Constants
import com.ohagner.deviations.api.watch.domain.Watch
import com.rabbitmq.client.Channel
import groovy.util.logging.Slf4j
import ratpack.exec.ExecController
import ratpack.exec.Execution
import ratpack.service.Service
import ratpack.service.StartEvent

import java.util.concurrent.TimeUnit

@Slf4j
class JobScheduler implements Service, Runnable {

    private static final int MAX_NUM_OF_WATCHES = 50

    WatchRepository watchRepository
    WatchProcessQueueingService queueingService

    @Inject
    JobScheduler(WatchRepository watchRepository, WatchProcessQueueingService queueingService) {
        this.watchRepository = watchRepository
        this.queueingService = queueingService
    }

    @Override
    public void onStart(StartEvent startEvent) {
        ExecController execController = startEvent.getRegistry().get(ExecController.class);
        long interval = AppConfig.envOrDefault("RATPACK_WATCH_PROCESS_JOB_INTERVAL_MINUTES", 5) as long
        execController.getExecutor()
                .scheduleAtFixedRate(this, 0, interval, TimeUnit.MINUTES)
        log.info "Scheduler started to run every ${interval} minutes"
    }

    @Override
    void run() {
        log.info "Watch process queueing started"
        Execution.fork()
                .onError { t -> log.error("Failed to submit watches for processing", t) }
                .start {
            List<Watch> watchesToProcess
            int pageNumber = 1
            while (watchesToProcess = watchRepository.retrieveRange(pageNumber, MAX_NUM_OF_WATCHES)) {
                log.debug "Retrieving range with pageNumber $pageNumber, got ${watchesToProcess.size()} watches"
                queueingService.enqueueForProcessing(watchesToProcess)
                pageNumber++
                log.info "Submitted ${watchesToProcess.size()} watches to watch processing queue"
            }
        }

    }

}


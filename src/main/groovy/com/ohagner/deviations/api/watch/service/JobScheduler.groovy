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
    Channel channel

    @Inject
    JobScheduler(WatchRepository watchRepository, Channel channel) {
        this.watchRepository = watchRepository
        this.channel = channel
    }

    @Override
    public void onStart(StartEvent startEvent) {
        ExecController execController = startEvent.getRegistry().get(ExecController.class);
        execController.getExecutor()
                .scheduleAtFixedRate(this, 0, AppConfig.envOrDefault("RATPACK_WATCH_PROCESS_JOB_INTERVAL_MINUTES", 5), TimeUnit.MINUTES)
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
                watchesToProcess.each {
                    channel.basicPublish("", Constants.WATCHES_TO_PROCESS_QUEUE_NAME, null, it.toJson().bytes)
                }
                pageNumber++
                log.info "Submitted ${watchesToProcess.size()} watches to watch processing queue"
            }
        }

    }

}


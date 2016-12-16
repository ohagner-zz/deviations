package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import groovy.util.logging.Slf4j

@Slf4j
class LoggingStage extends WatchProcessingStage {

    @Override
    void performAction(WatchProcessingData data) {
        log.info("Processed watch ${data.watch.name}: ${data.result.toString()}")
    }
}

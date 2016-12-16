package com.ohagner.deviations.worker.watch.service

import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.domain.WatchProcessingResult
import com.ohagner.deviations.worker.watch.service.stage.WatchProcessingStage
import groovy.util.logging.Slf4j

@Slf4j
class WatchProcessingChain {

    List<WatchProcessingStage> stages = new LinkedList<>()

    void process(Watch watchToProcess) {
        if(stages.isEmpty()) {
            throw new IllegalStateException("At least one stage must be added to chain")
        }

        try {
            stages.first.execute(new WatchProcessingData(watch: watchToProcess, result: WatchProcessingResult.newInstance()))
        } catch(Exception e) {
            log.error("Watch processing failed", e)
        }
    }

    void appendStage(WatchProcessingStage stage) {
        if(!stages.isEmpty()) {
            stages.last.successor = stage
        }
        stages.add(stage)
    }

}

package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.worker.watch.domain.WatchProcessingData

abstract class WatchProcessingStage {

    WatchProcessingStage successor

    final void execute(WatchProcessingData watchProcessingData) {
        performAction(watchProcessingData)
        if(successor) {
            successor.execute(watchProcessingData)
        }
    }

    void performAction(WatchProcessingData watchProcessingData) {}

}
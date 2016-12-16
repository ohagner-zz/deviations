package com.ohagner.deviations.worker.watch.service

import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.service.stage.WatchProcessingStage

class NameAddingStage extends WatchProcessingStage {

    String name

    @Override
    void performAction(WatchProcessingData data) {
        data.watch.name += name
    }
}
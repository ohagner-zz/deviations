package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus

import java.time.LocalDateTime

import static com.ohagner.deviations.config.Constants.ZONE_ID

class TimeToCheckStage extends WatchProcessingStage {

    @Override
    void performAction(WatchProcessingData data) {

        if (data.watch.isTimeToCheck(LocalDateTime.now(ZONE_ID))) {
            data.result.status = WatchProcessingStatus.TIME_TO_CHECK
            data.result.addMessage("Time to check")
        } else {
            data.result.status = WatchProcessingStatus.NOT_TIME_TO_CHECK
            data.result.addMessage("Not time to check")
        }
    }
}

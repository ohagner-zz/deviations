package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.worker.api.service.DeviationsApiClient
import com.ohagner.deviations.worker.watch.domain.WatchProcessingData

import java.time.LocalDateTime

import static com.ohagner.deviations.config.Constants.ZONE_ID
import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.UPDATED
import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.UPDATE_FAILED

class UpdatingStage extends WatchProcessingStage {

    DeviationsApiClient deviationsApiClient

    @Override
    void performAction(WatchProcessingData data) {
        data.watch.lastProcessed = LocalDateTime.now(ZONE_ID)
        boolean updateWasSuccessful = deviationsApiClient.update(data.watch)
        data.result.status = updateWasSuccessful ? UPDATED : UPDATE_FAILED
    }

}

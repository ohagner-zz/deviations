package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.config.Constants
import com.ohagner.deviations.worker.api.service.DeviationsApiClient
import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus

import java.time.LocalDateTime

import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.*

class NotificationStage extends WatchProcessingStage {

    DeviationsApiClient deviationsApiClient

    @Override
    void performAction(WatchProcessingData data) {
        if(statusApplicableForProcessing(data.result.status)) {
            boolean notificationSent = deviationsApiClient.sendNotifications(data.watch, data.result.matchingDeviations)
            if(notificationSent) {
                data.watch.lastNotified = LocalDateTime.now(Constants.ZONE_ID)
                data.result.addMessage("Sent notification")
                data.result.status = NOTIFIED
            } else {
                data.result.status = NOTIFICATION_FAILED
                data.result.addMessage("Failed to send notifications")
            }
        }
    }

    private boolean statusApplicableForProcessing(WatchProcessingStatus status) {
        return status == MATCHED
    }
}

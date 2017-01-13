package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.api.deviation.service.DeviationMatcher
import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus

import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.MATCHED
import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.NO_MATCH
import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.TIME_TO_CHECK

class DeviationMatchingStage extends WatchProcessingStage {

    private DeviationMatcher deviationMatcher

    @Override
    void performAction(WatchProcessingData data) {
        if(statusApplicableForProcessing(data.result.status)) {
            data.result.matchingDeviations = deviationMatcher.findMatching(data.watch)
            if(data.result.matchingDeviations) {
                data.watch.addProcessedDeviationIds(data.result.matchingDeviations.collect { it.id })
                data.result.status = MATCHED
                data.result.addMessage("Found ${data.result.matchingDeviations.size()} matching deviations")
            } else {
                data.result.status = NO_MATCH
                data.result.addMessage("No matching deviations found")
            }
        }
    }

    private boolean statusApplicableForProcessing(WatchProcessingStatus status) {
        return status == TIME_TO_CHECK
    }

}

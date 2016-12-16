package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.api.deviation.domain.Deviation
import com.ohagner.deviations.api.deviation.service.DeviationMatcher
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.domain.WatchProcessingResult
import com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus
import spock.lang.Specification
import spock.lang.Unroll

import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.*

class DeviationMatchingStageSpec extends Specification {

    DeviationMatcher deviationMatcher = Mock(DeviationMatcher)
    DeviationMatchingStage stage = new DeviationMatchingStage(deviationMatcher: deviationMatcher)

    WatchProcessingData data = new WatchProcessingData(watch: new Watch(), result: WatchProcessingResult.newInstance())

    void 'should match deviations'() {
        when:
            data.result.status = TIME_TO_CHECK
            stage.execute(data)
        then:
            1 * deviationMatcher.findMatching(_) >> [new Deviation(id: "1")]
            data.result.status == MATCHED
            data.watch.processedDeviationIds.contains("1")
    }

    void 'should not match deviations'() {
        when:
            data.result.status = TIME_TO_CHECK
            stage.execute(data)
        then:
            1 * deviationMatcher.findMatching(_) >> []
            data.result.status == NO_MATCH
    }

    @Unroll
    void 'should not process #currentStatus'() {
        given:
            data.result.status = currentStatus
        when:
            stage.execute(data)
        then:
            0 * deviationMatcher.findMatching(_)
            data.result.status == currentStatus
        where:
            currentStatus << (WatchProcessingStatus.values() - TIME_TO_CHECK)
    }

}

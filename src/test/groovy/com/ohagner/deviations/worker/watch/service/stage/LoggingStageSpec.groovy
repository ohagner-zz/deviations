package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.domain.WatchProcessingResult
import spock.lang.Specification

class LoggingStageSpec extends Specification {

    LoggingStage stage = new LoggingStage()
    WatchProcessingData data = new WatchProcessingData(watch: new Watch(), result: WatchProcessingResult.newInstance())

    void 'test that result can be logged'() {
        expect:
            stage.execute(data)
    }
}

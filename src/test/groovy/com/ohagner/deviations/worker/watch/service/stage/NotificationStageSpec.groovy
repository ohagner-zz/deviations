package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.worker.api.service.DeviationsApiClient
import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.domain.WatchProcessingResult
import com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus
import spock.lang.Specification
import spock.lang.Unroll

import static com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus.*

class NotificationStageSpec extends Specification {

    DeviationsApiClient client = Mock()
    NotificationStage stage = new NotificationStage(deviationsApiClient: client)

    WatchProcessingData data = new WatchProcessingData(watch: new Watch(), result: WatchProcessingResult.newInstance())

    void 'should set correct status when notifications succeeds'() {
        when:
            data.result.status = MATCHED
            stage.execute(data)
        then:
            1 * client.sendNotifications(data.watch, data.result.matchingDeviations) >> true
            data.result.status == NOTIFIED
            data.watch.lastNotified != null
    }

    void 'should set correct status when notifications fails'() {
        when:
            data.result.status = MATCHED
            stage.execute(data)
        then:
            1 * client.sendNotifications(data.watch, data.result.matchingDeviations) >> false
            data.result.status == NOTIFICATION_FAILED
            data.watch.lastNotified == null
    }

    @Unroll
    void 'should not process #currentStatus'() {
        given:
            data.result.status = currentStatus
        when:
            stage.execute(data)
        then:
            0 * client.sendNotifications(_,_)
        where:
            currentStatus << (WatchProcessingStatus.values() - MATCHED)
    }
}

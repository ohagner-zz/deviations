package com.ohagner.deviations.worker.watch.service.stage

import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.domain.schedule.WeeklySchedule
import com.ohagner.deviations.worker.watch.domain.WatchProcessingData
import com.ohagner.deviations.worker.watch.domain.WatchProcessingResult
import com.ohagner.deviations.worker.watch.domain.WatchProcessingStatus
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime

import static com.ohagner.deviations.config.Constants.ZONE_ID

class TimeToCheckStageSpec extends Specification {

    TimeToCheckStage stage = new TimeToCheckStage()

    void 'should set status to time to check'() {
        given:
            WatchProcessingData data = new WatchProcessingData(watch: timeToCheckWatch(), result: WatchProcessingResult.newInstance())
        when:
            stage.execute(data)
        then:
            data.result.status == WatchProcessingStatus.TIME_TO_CHECK
    }

    void 'should set status to not time to check'() {
        given:
            WatchProcessingData data = new WatchProcessingData(watch: notTimeToCheckWatch(), result: WatchProcessingResult.newInstance())
        when:
            stage.execute(data)
        then:
            data.result.status == WatchProcessingStatus.NOT_TIME_TO_CHECK
    }

    private Watch timeToCheckWatch() {
        def matching = new Watch(name: "matching", notifyMaxHoursBefore: 1)
        matching.schedule = new WeeklySchedule(timeOfEvent: LocalTime.now(ZONE_ID).plusMinutes(5), weekDays: [LocalDate.now(ZONE_ID).dayOfWeek])
        return matching
    }

    private Watch notTimeToCheckWatch() {
        def nonMatching = new Watch(name: "nonMatching", notifyMaxHoursBefore: 1)
        nonMatching.schedule = new WeeklySchedule(timeOfEvent: LocalTime.now(ZONE_ID).plusHours(2), weekDays: [LocalDate.now(ZONE_ID).dayOfWeek])
        return nonMatching
    }

}

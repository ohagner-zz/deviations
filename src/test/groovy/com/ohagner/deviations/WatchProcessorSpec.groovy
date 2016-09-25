package com.ohagner.deviations

import com.ohagner.deviations.domain.Transport
import com.ohagner.deviations.domain.TransportMode
import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.domain.notifications.NotificationType
import com.ohagner.deviations.domain.schedule.SingleOccurrence
import com.ohagner.deviations.notifications.LogNotifier
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.task.WatchExecutionStatus
import com.ohagner.deviations.watch.WatchProcessor
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Test job for matching watches against current deviations
 */
class WatchProcessorSpec extends Specification {



    void 'should match deviations'() {
        given:
            UserRepository userRepo = new UserRepository(null) {
                @Override
                User findByUsername(String username) {
                    return new User(username: "user", emailAddress: "emailAddress")
                }
            }
            NotificationService notificationService = new NotificationService([new LogNotifier()], userRepo)
            WatchProcessor watchProcessor = WatchProcessor.builder()
                .notificationService(notificationService)
                .deviationMatcher(new DeviationMatcher(createDeviationList()))
                .watchesToProcess(createWatches()).build()
        when:
            def resultMap = watchProcessor.process()
        then:
            assert resultMap.get(WatchExecutionStatus.NOTIFIED).size() == 1
    }

    List<Deviation> createDeviationList() {
        def deviation = new Deviation()
        deviation.header = "header"
        deviation.lineNumbers = ["35", "36"]
        deviation.transportMode = TransportMode.BUS
        deviation.details = "details"
        deviation.from = LocalDateTime.now()
        deviation.to = LocalDateTime.now()
        deviation.created = LocalDateTime.now()
        deviation.updated = LocalDateTime.now()
        return [deviation]
    }

    List<Watch> createWatches() {
        def matching = new Watch(name: "matching", notifyMaxHoursBefore: 1, notifyBy: [NotificationType.LOG], transports: [new Transport(line: "35", transportMode: TransportMode.BUS)])
        matching.schedule = new SingleOccurrence(timeOfEvent: LocalTime.now().plusMinutes(5), dateOfEvent: LocalDate.now())
        def nonMatching = new Watch(name: "nonMatching", notifyMaxHoursBefore: 1, notifyBy: [NotificationType.LOG], transports: [new Transport(line: "99", transportMode: TransportMode.TRAIN)])
        nonMatching.schedule = new SingleOccurrence(timeOfEvent: LocalTime.now().plusMinutes(5l), dateOfEvent: LocalDate.now())
        return [matching, nonMatching]
    }
}

package com.ohagner.deviations.scheduler

import com.google.inject.Guice
import com.google.inject.Injector
import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.DeviationRepo
import com.ohagner.deviations.HttpDeviationRepo
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.modules.MongoModule
import com.ohagner.deviations.notifications.EmailNotifier
import com.ohagner.deviations.notifications.LogNotifier
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
import com.ohagner.deviations.watch.WatchProcessor
import groovy.util.logging.Slf4j
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory

@Slf4j
class JobScheduler {

    def static main(def args) {

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler.start()
        JobDetail jobDetail = JobBuilder.newJob(WatchProcessingJob).build()
        Trigger trigger = TriggerBuilder.newTrigger()
            .startNow()
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(10))
            .build()
        scheduler.scheduleJob(jobDetail, trigger)
    }


}

@Slf4j
public class WatchProcessingJob implements Job {

    @Override
    void execute(JobExecutionContext context) throws JobExecutionException {
        Injector injector = Guice.createInjector(new MongoModule())
        WatchRepository watchRepository = injector.getInstance(WatchRepository)
        UserRepository userRepository = injector.getInstance(UserRepository)
        DeviationRepo deviationRepo = new HttpDeviationRepo()
        List<Watch> watches = watchRepository.retrieveAll()
        DeviationMatcher deviationMatcher = new DeviationMatcher(deviationRepo.retrieveAll())
        WatchProcessor processor = WatchProcessor.builder()
                .notificationService(new NotificationService([new LogNotifier(), new EmailNotifier()], userRepository))
                .deviationMatcher(deviationMatcher)
                .watchesToProcess(watches).build()
        log.info "LoggingJob executing!"
        processor.process()
    }
}
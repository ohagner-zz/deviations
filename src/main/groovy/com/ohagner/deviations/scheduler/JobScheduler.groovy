package com.ohagner.deviations.scheduler

import com.google.inject.Guice
import com.google.inject.Injector
import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.modules.TrafikLabModule
import com.ohagner.deviations.repository.CachedDeviationRepository
import com.ohagner.deviations.repository.DeviationRepository
import com.ohagner.deviations.repository.HttpDeviationRepository
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.modules.MongoModule
import com.ohagner.deviations.notifications.EmailNotifier
import com.ohagner.deviations.notifications.LogNotifier
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
import com.ohagner.deviations.watch.WatchProcessor
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory

@Slf4j
class JobScheduler {

    static Injector mongoInjector
    static Injector trafikLabInjector

    def static main(def args) {

        mongoInjector = Guice.createInjector(new MongoModule())
        trafikLabInjector = Guice.createInjector(new TrafikLabModule())

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler.start()
        JobDetail jobDetail = JobBuilder
                .newJob(WatchProcessingJob)
                .build()
        Trigger trigger = TriggerBuilder.newTrigger()
            .startNow()
            .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(30))
            .build()
        scheduler.scheduleJob(jobDetail, trigger)
    }


}

@Slf4j
@CompileStatic
public class WatchProcessingJob implements Job {

    @Override
    void execute(JobExecutionContext context) throws JobExecutionException {
//        Injector mongoInjector = Guice.createInjector(new MongoModule())
//        Injector trafikLabInjector = Guice.createInjector(new TrafikLabModule())

        WatchRepository watchRepository = JobScheduler.mongoInjector.getInstance(WatchRepository)
        UserRepository userRepository = JobScheduler.mongoInjector.getInstance(UserRepository)

        DeviationRepository deviationRepo = JobScheduler.trafikLabInjector.getInstance(DeviationRepository)
        DeviationMatcher deviationMatcher = new DeviationMatcher(deviationRepo.retrieveAll())

        WatchProcessor processor = WatchProcessor.builder()
                .notificationService(new NotificationService([new LogNotifier(), new EmailNotifier()], userRepository))
                .watchRepository(watchRepository)
                .deviationMatcher(deviationMatcher).build()
        log.info "LoggingJob executing!"
        processor.process()
    }
}
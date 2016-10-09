package com.ohagner.deviations.scheduler

import com.ohagner.deviations.domain.Watch
import groovy.util.logging.Slf4j
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.RESTClientException
import wslite.rest.Response

@Slf4j
class JobScheduler {


    def static main(def args) {
        def response2 = "http://localhost:5000/admin/watchesToProcess".toURL().getText("UTF-8")
        log.info "Response $response2"
        RESTClient restClient = new RESTClient("http://localhost:5000")
        try {
            def response = restClient.get(path: "admin/watchesToProcess", accept: ContentType.JSON)
            println response.contentAsString
            List<Watch> watches = response.json.collect { Watch.fromJson(it) }
        } catch(RESTClientException rce) {
            println "Hej hej " + rce.message
            rce.printStackTrace()
        }
//        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler()
//        scheduler.start()
//        JobDetail jobDetail = JobBuilder
//                .newJob(WatchProcessingJob)
//                .build()
//        Trigger trigger = TriggerBuilder.newTrigger()
//            .startNow()
//            .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(5))
//            .build()
//        scheduler.scheduleJob(jobDetail, trigger)
    }

}

@Slf4j
public class WatchProcessingJob implements Job {

    @Override
    void execute(JobExecutionContext context) throws JobExecutionException {

        RESTClient restClient = new RESTClient("http://localhost:5050")
        Response response = restClient.get(path: "/admin/watchesToProcess", contentType: ContentType.JSON)
        List<Watch> watches = response.json.collect { Watch.fromJson(it)}
    }
}
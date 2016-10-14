package com.ohagner.deviations.watch.task

import com.google.inject.Guice
import com.google.inject.Injector
import com.ohagner.deviations.DeviationMatcher
import com.ohagner.deviations.config.AppConfig
import com.ohagner.deviations.config.Constants
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.modules.DeviationsModule
import com.ohagner.deviations.modules.MessagingModule
import com.ohagner.deviations.repository.DeviationRepository
import com.ohagner.deviations.watch.WatchProcessor
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope

import groovy.transform.builder.Builder
import groovy.util.logging.Slf4j
import wslite.rest.RESTClient


@Slf4j
@Builder
class WatchProcessingWorker  {

    RESTClient apiClient
    DeviationRepository deviationRepository


    def static main(args) {
        String apiBaseUrl = AppConfig.envOrProperty("DEVIATIONS_API_URL")
        if(!apiBaseUrl) {
            log.error "No url found for api. Exiting..."
            System.exit(-1)
        }

        Injector deviationsInjector = Guice.createInjector(new DeviationsModule())
        DeviationRepository deviationRepository = deviationsInjector.getInstance(DeviationRepository)

        Injector messagingInjector = Guice.createInjector(new MessagingModule())
        Channel channel = messagingInjector.getInstance(Channel)

        WatchProcessingWorker worker = new WatchProcessingWorker(apiClient: new RESTClient(apiBaseUrl), deviationRepository: deviationRepository)
        try {
            worker.handleIncomingWork(channel)
        } catch(Exception e) {
            log.error("Watch processing worker process failed", e)
        }

    }

    private void handleIncomingWork(Channel channel) {

        Consumer consumer = new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                log.info "Matching deviation"
                Watch watch = Watch.fromJson(message)
                DeviationMatcher deviationMatcher = new DeviationMatcher(deviationRepository.retrieveAll())
                WatchProcessor watchProcessor = WatchProcessor.builder().deviationsApiClient(apiClient).deviationMatcher(deviationMatcher).build()
                WatchProcessingResult result = watchProcessor.process(watch)
                log.info "Watchprocessor result:\n${result.toString()}"
            }


        }
        channel.basicConsume(Constants.WATCHES_TO_PROCESS_QUEUE_NAME, true, consumer)
        println "Started consumer"
    }

}

package com.ohagner.deviations.api.watch.service

import com.google.inject.Inject
import com.ohagner.deviations.api.watch.domain.Watch
import com.rabbitmq.client.Channel

import static com.ohagner.deviations.config.Constants.WATCHES_TO_PROCESS_QUEUE_NAME

class WatchProcessQueueingService {

    Channel channel

    @Inject
    WatchProcessQueueingService(Channel channel) {
        this.channel = channel
    }

    public void enqueueForProcessing(Watch watch) {
        channel.basicPublish("", WATCHES_TO_PROCESS_QUEUE_NAME, null, watch.toJson().bytes)
    }

    public void enqueueForProcessing(List<Watch> watches) {
        watches.each { enqueueForProcessing(it) }
    }
}

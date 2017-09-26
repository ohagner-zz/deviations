package com.ohagner.deviations.api.deviation.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.watch.repository.WatchRepository
import com.ohagner.deviations.api.watch.service.WatchProcessQueueingService
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.jackson.Jackson.json

@Slf4j
class DeviationCheckHandler extends GroovyHandler {

    WatchRepository watchRepository
    WatchProcessQueueingService queueingService

    @Inject
    DeviationCheckHandler(WatchRepository watchRepository, WatchProcessQueueingService queueingService) {
        this.watchRepository = watchRepository
        this.queueingService = queueingService
    }

    @Override
    protected void handle(GroovyContext context) {
        User loggedInUser = context.get(User)
        log.debug "Retrieving watches for user ${loggedInUser.credentials.username}"
        watchRepository.findByUsername(loggedInUser.credentials.username)
            .route({ watchList -> watchList.isEmpty()}) {
                String message = "No watches found to check"
                context.render json(["message": message])
            }.then { watchesToCheck ->
                queueingService.enqueueForProcessing(watchesToCheck)
                String message = "Started deviation checking for ${watchesToCheck.size()} ${watchesToCheck.size() == 1 ? 'watch' : 'watches'}"
                context.render json(["message": message])
            }
    }
}

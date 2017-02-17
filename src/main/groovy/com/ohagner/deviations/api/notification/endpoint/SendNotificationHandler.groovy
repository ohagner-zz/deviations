package com.ohagner.deviations.api.notification.endpoint

import com.google.inject.Inject
import com.ohagner.deviations.api.notification.domain.Notification
import com.ohagner.deviations.api.notification.service.NotificationService
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import static ratpack.jackson.Jackson.json

@Slf4j
class SendNotificationHandler extends GroovyHandler {

    UserRepository userRepository
    NotificationService notificationService

    @Inject
    SendNotificationHandler(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository
        this.notificationService = notificationService
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            String username = pathTokens.username
            log.info "Username: $username"
            Optional<User> userOptional = userRepository.findByUsername(username)
            if(userOptional.present) {
                User user = userOptional.get()
                request.body.map { body ->
                    log.info "Notification payload ${body.text}"
                    Notification notification = Notification.fromJson(body.text)
                    notificationService.sendNotification(user, notification)
                }.onError { t ->
                    log.error("Failure to send notifications", t)
                    response.status(500)
                    render json([message: "Notification failed"])
                }.then {
                    response.status(204)
                    response.send()
                }
            } else {
                response.status(404)
                render json([message:"Failed to send notifications. User $username could not be found"])
            }
        }
    }
}

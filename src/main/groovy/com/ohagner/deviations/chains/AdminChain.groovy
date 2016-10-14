package com.ohagner.deviations.chains

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.notifications.Notification
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.UserRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class AdminChain extends GroovyChainAction {
    @Override
    void execute() throws Exception {
        post("users/:username/notification") { UserRepository userRepository,  NotificationService notificationService ->
            log.info "Handling notification"
            String username = pathTokens.username
            Optional<User> userOptional = userRepository.findByUsername(username)
            if(userOptional.present) {
                log.info "User is present, preparing to notify"
                User user = userOptional.get()
                request.body.map { body ->
                    log.info "Request body = ${body.text}"
                    Notification notification = Notification.fromJson(body.text)
                    notificationService.sendNotification(user, notification)
                }.onError { t ->
                    log.error("Notification failed", t)
                    response.status(500)
                    render json([message: "Notification failed"])
                }.then {
                    response.status(204)
                    response.send()
                }
            } else {
                response.status(404)
                render json([message:"User $username not found"])
            }
        }
    }
}

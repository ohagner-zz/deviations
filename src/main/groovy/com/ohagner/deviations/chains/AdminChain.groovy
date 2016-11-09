package com.ohagner.deviations.chains

import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.domain.notification.Notification
import com.ohagner.deviations.handlers.AdminAuthorizationHandler
import com.ohagner.deviations.handlers.UserAuthorizationHandler
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.UserRepository
import com.ohagner.deviations.repository.WatchRepository
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class AdminChain extends GroovyChainAction {
    @Override
    void execute() throws Exception {
        post("users/:username/notification") { UserRepository userRepository,  NotificationService notificationService ->
            String username = pathTokens.username
            Optional<User> userOptional = userRepository.findByUsername(username)
            if(userOptional.present) {
                User user = userOptional.get()
                request.body.map { body ->
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
        delete("users/:username") { UserRepository userRepository, WatchRepository watchRepository ->
            String username = pathTokens.username
            log.debug "Deleting user"
            Optional<User> optUser = userRepository.findByUsername(username)
            if(optUser.isPresent()) {
                User user = optUser.get()
                List<Watch> watches = watchRepository.findByUsername(user.credentials.username)
                watches.each { Watch watch -> watchRepository.delete(user.credentials.username, watch.name) }
                userRepository.delete(user)
                render json(user)
            } else {
                response.status(404)
                render json([message: "User could not be found"])
            }

        }
    }
}

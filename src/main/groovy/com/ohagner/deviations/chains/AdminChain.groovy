package com.ohagner.deviations.chains

import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.domain.notification.Notification
import com.ohagner.deviations.handlers.AdminAuthorizationHandler
import com.ohagner.deviations.handlers.UserAuthorizationHandler
import com.ohagner.deviations.handlers.notification.SendNotificationHandler
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
        post("users/:username/notification", SendNotificationHandler)
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

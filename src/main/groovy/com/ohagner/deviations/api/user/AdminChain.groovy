package com.ohagner.deviations.api.user

import com.ohagner.deviations.api.watch.Watch
import com.ohagner.deviations.api.watch.UpdateWatchHandler
import com.ohagner.deviations.api.notification.SendNotificationHandler
import com.ohagner.deviations.api.watch.WatchRepository
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
                watches.each { Watch watch -> watchRepository.delete(user.credentials.username, watch.id) }
                userRepository.delete(user)
                render json(user)
            } else {
                response.status(404)
                render json([message: "User could not be found"])
            }

        }
        put("watches/:id") { WatchRepository watchRepository ->
            insert(new UpdateWatchHandler(watchRepository))
        }
    }
}

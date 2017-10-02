package com.ohagner.deviations.api.user.router

import com.ohagner.deviations.api.notification.endpoint.SendNotificationHandler
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.endpoint.UpdateWatchHandler
import com.ohagner.deviations.api.watch.repository.WatchRepository
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import ratpack.groovy.handling.GroovyChainAction

import static ratpack.jackson.Jackson.json

@Slf4j
class AdminChain extends GroovyChainAction {
    @Override
    void execute() throws Exception {
        post("users/:username/notification", SendNotificationHandler)

        //TODO: Move to handler
        delete("users/:username") { UserRepository userRepository, WatchRepository watchRepository ->
            String username = pathTokens.username
            log.info "Deleting user"
            userRepository.findByUsername(username)
                .onNull {
                    response.status(204)
                    render json([message: "User could not be found"])
                }.flatRight { user ->
                    log.info "flatRight here with user : ${user.credentials.username}"
                    watchRepository.findByUsername(user.credentials.username)
                }.flatMap { pair ->
                    List<Watch> watches = pair.right
                    User user = pair.left
                    watches.each { Watch watch ->
                        watchRepository.delete(user.credentials.username, watch.id).then {
                            log.info "Deleted watch ${watch.id}"
                        }
                    }
                    userRepository.delete(user)
                }.then { user ->
                    render json(user)
                }
        }

        put("watches/:id") { WatchRepository watchRepository ->
            insert(new UpdateWatchHandler(watchRepository))
        }
    }
}

package com.ohagner.deviations.chains

import com.ohagner.deviations.api.user.domain.Credentials
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import com.ohagner.deviations.api.user.router.AdminChain
import com.ohagner.deviations.api.notification.endpoint.SendNotificationHandler
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.repository.WatchRepository
import ratpack.exec.Promise
import ratpack.groovy.test.handling.GroovyRequestFixture
import ratpack.handling.Handlers
import ratpack.jackson.JsonRender
import spock.lang.Specification

class AdminChainSpec extends Specification {


    void 'should call SendNotificationHandler'() {
        when:
            def result = GroovyRequestFixture
                    .requestFixture()
                    .handle(new AdminChain()) {
                uri "users/username/notification"
                method "POST"
                registry.add(SendNotificationHandler, Handlers.post())
            }
        then:
            result.isCalledNext()
    }

    void 'should delete user and all associated watches'() {
        given:
            UserRepository userRepository = Mock()
            WatchRepository watchRepository = Mock()
        when:
            userRepository.findByUsername(_) >> Promise.value(new User(credentials: new Credentials(username: "username")))
            1 * userRepository.delete(_)
            1 * watchRepository.findByUsername(_) >> Promise.value([new Watch(id: 111L), new Watch(id: 222L)])
            2 * watchRepository.delete(_, _) >> Promise.value(Optional.empty())
            def result = GroovyRequestFixture
                    .requestFixture()
                    .pathBinding(["username": "username"])
                    .handle(new AdminChain()) {
                uri "users/username"
                method "delete"
                registry.add(UserRepository, userRepository)
                registry.add(WatchRepository, watchRepository)
                registry.add(SendNotificationHandler, Handlers.post())
            }
        then:
            assert result.rendered(JsonRender).object.credentials.username == "username"
    }


    void 'should give 404 when user is not found'() {
        given:
            UserRepository userRepository = Mock()
            WatchRepository watchRepository = Mock()
        when:
            userRepository.findByUsername(_) >> Promise.value(null)
            0 * userRepository.delete(_)
            def result = GroovyRequestFixture
                    .requestFixture()
                    .pathBinding(["username": "username"])
                    .handle(new AdminChain()) {
                uri "users/username"
                method "delete"
                registry.add(UserRepository, userRepository)
                registry.add(WatchRepository, watchRepository)
                registry.add(SendNotificationHandler, Handlers.post())
            }
        then:
            assert result.status.code == 404
    }
}
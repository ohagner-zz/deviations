package com.ohagner.deviations.api.deviation.endpoint

import com.ohagner.deviations.api.user.domain.Credentials
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.repository.WatchRepository
import com.ohagner.deviations.api.watch.service.WatchProcessQueueingService
import ratpack.exec.Promise
import ratpack.groovy.test.handling.GroovyRequestFixture
import ratpack.jackson.JsonRender
import spock.lang.Specification

class DeviationCheckHandlerSpec extends Specification {

    GroovyRequestFixture requestFixture

    WatchRepository mockWatchRepository
    WatchProcessQueueingService mockQueueingService

    DeviationCheckHandler handler

    def setup() {
        mockWatchRepository = Mock()
        mockQueueingService = Mock()
        handler = new DeviationCheckHandler(mockWatchRepository, mockQueueingService)
        requestFixture = GroovyRequestFixture.requestFixture()
                .registry { registry ->
            registry.add(WatchRepository, mockWatchRepository)
            registry.add(WatchProcessQueueingService, mockQueueingService)
            registry.add(User, new User(credentials: new Credentials(username: "username")))
        }
    }

    void 'should start deviation check process for logged in user'() {
        given:
            1 * mockWatchRepository.findByUsername("username") >> Promise.value([new Watch(), new Watch()])
            1 * mockQueueingService.enqueueForProcessing(_)
        expect:
                def result = requestFixture.handle(handler)
                assert result.rendered(JsonRender).object.message == "Started deviation checking for 2 watches"


    }

    void 'should not start deviation check process when logged in user has no watches'() {
        given:
            1 * mockWatchRepository.findByUsername("username") >> Promise.value([])
            0 * mockQueueingService.enqueueForProcessing(_)
        expect:
            def result = requestFixture.handle(handler)
            assert result.rendered(JsonRender).object.message == "No watches found to check"


    }

}

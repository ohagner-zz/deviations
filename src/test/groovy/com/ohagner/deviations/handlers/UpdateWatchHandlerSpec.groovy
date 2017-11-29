package com.ohagner.deviations.handlers

import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.endpoint.UpdateWatchHandler
import com.ohagner.deviations.api.watch.repository.WatchRepository
import ratpack.exec.Promise
import ratpack.groovy.test.handling.GroovyRequestFixture
import ratpack.jackson.JsonRender
import spock.lang.Specification

class UpdateWatchHandlerSpec extends Specification {


    WatchRepository watchRepository = Mock()
    UpdateWatchHandler handler = new UpdateWatchHandler(watchRepository)

    GroovyRequestFixture requestFixture = GroovyRequestFixture.requestFixture()
        .body(new Watch(id: 1l).toJson(), "application/json")

    void 'should update watch'() {
        when:
            def result = requestFixture.pathBinding(["id":"1"]).handle(handler)
        then:
            1 * watchRepository.update(_) >> Promise.value(new Watch(id: 1l))
            result.status.'2xx'
            result.rendered(JsonRender).object.id == 1
    }

    void 'should handle repository exception'() {
        when:
            def result = requestFixture.pathBinding(["id":"1"]).handle(handler)
        then:
            1 * watchRepository.update(_) >> { throw new Exception()}
            result.status.'5xx'
    }

}

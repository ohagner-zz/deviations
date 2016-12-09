package com.ohagner.deviations.handlers

import com.ohagner.deviations.api.watch.UpdateWatchHandler
import com.ohagner.deviations.api.watch.Watch
import com.ohagner.deviations.api.watch.WatchRepository
import ratpack.groovy.test.handling.GroovyRequestFixture
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
            1 * watchRepository.update(_) >> new Watch(id: "1")
            result.status.'2xx'
    }

    void 'should fail when id does not match'() {
        when:
            def result = requestFixture.pathBinding(["id":"999"]).handle(handler)
        then:
            0 * watchRepository.update(_)
            result.status.'5xx'
    }

    void 'should handle repository exception'() {
        when:
            def result = requestFixture.pathBinding(["id":"1"]).handle(handler)
        then:
            1 * watchRepository.update(_) >> { throw new Exception()}
            result.status.'5xx'
    }

}

package com.ohagner.deviations.api.watch.router

import com.ohagner.deviations.api.user.domain.Credentials
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.watch.domain.Watch
import com.ohagner.deviations.api.watch.repository.WatchRepository
import ratpack.exec.Promise
import ratpack.groovy.test.handling.GroovyRequestFixture
import ratpack.jackson.JsonRender
import ratpack.test.handling.HandlingResult
import static ratpack.http.MediaType.APPLICATION_JSON
import spock.lang.Specification

class WatchChainSpec extends Specification {

    GroovyRequestFixture fixture = GroovyRequestFixture.requestFixture()
    WatchRepository watchRepository = Mock()

    void 'should create watch'() {
        given:
            Watch toCreate = new Watch(id: 1, name: "TestWatch")
            watchRepository.create(_) >> Promise.value(Optional.of(toCreate))
        when:
            HandlingResult result = fixture
                    .uri("")
                    .method("POST")
                    .body(toCreate.toJson(), APPLICATION_JSON)
                    .registry {
                        add(User, new User(credentials: new Credentials(username: "username")))
                        add(WatchRepository, watchRepository)
                    }
                    .handleChain(new WatchChain())
        then:
            result.status.'2xx'
            result.rendered(JsonRender).object == toCreate
    }

    void 'should give 500 when watch could not be created'() {
        given:
            Watch toCreate = new Watch(name: "TestWatch")
            watchRepository.create(_) >> Promise.value(Optional.empty())
        when:
            HandlingResult result = fixture
                    .uri("")
                    .method("POST")
                    .body(toCreate.toJson(), APPLICATION_JSON)
                    .registry {
                add(User, new User(credentials: new Credentials(username: "username")))
                add(WatchRepository, watchRepository)
            }
            .handleChain(new WatchChain())
        then:
            result.status.'5xx'
            result.rendered(JsonRender).object.message.contains("Failed to create")
    }

    void 'should return list of users watches'() {
        given:
            watchRepository.findByUsername(_) >> Promise.value([new Watch(id: 1), new Watch(id: 2)])
        when:
            HandlingResult result = fixture.handle(new WatchChain()) {
                method "GET"
                uri ""
                registry.add(User, new User(credentials: new Credentials(username: "username")))
                registry.add(WatchRepository, watchRepository)
            }
        then:
            result.status.'2xx'
            result.rendered(JsonRender).object.size() == 2
    }


}

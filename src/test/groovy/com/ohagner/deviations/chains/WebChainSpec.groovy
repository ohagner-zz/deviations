package com.ohagner.deviations.chains

import com.ohagner.deviations.handlers.UserAuthorizationHandler
import com.ohagner.deviations.repository.DeviationRepository
import com.ohagner.deviations.web.service.UserService
import ratpack.exec.Promise
import ratpack.http.Status
import ratpack.session.Session
import ratpack.test.handling.RequestFixture
import spock.lang.Specification

import static ratpack.http.MediaType.APPLICATION_FORM

class WebChainSpec extends Specification {

    RequestFixture requestFixture

    Session session
    UserService userService

    public static final Map EXISTING_USER = [username: "username", firstName: "firstName", lastName: "lastName", emailAddress: "emailAddress"]

    def setup() {

        session = Mock(Session)
        userService = Mock(UserService)
        requestFixture = RequestFixture.requestFixture()
                .registry { registry ->
            registry.add(UserService, userService)
            registry.add(Session, session)
        }
    }

    void 'should update user'() {
        given:
            def formMap = [username: "updatedUsername", firstName: "updatedFirstName", lastName: "updatedLastName", emailAddress: "updatedEmailAddress"]
        when:
            def result = requestFixture
                    .uri("user/update")
                    .method("POST")
                    .body(formMap.collect { it }.join('&'), APPLICATION_FORM)
                    .handleChain(new WebChain())
        then:
            2 * session.require(_) >> Promise.value(EXISTING_USER)
            1 * userService.update(_) >> Promise.value(true)
            assert result.sentResponse == true
            assert result.status.'3xx'
    }

    void 'should inform that update failed'() {
        given:
            def formMap = [username: "updatedUsername", firstName: "updatedFirstName", lastName: "updatedLastName", emailAddress: "updatedEmailAddress"]
        when:
            def result = requestFixture
                    .uri("user/update")
                    .method("POST")
                    .body(formMap.collect { it }.join('&'), APPLICATION_FORM)
                    .handleChain(new WebChain())
        then:
            2 * session.require(_) >> Promise.value(EXISTING_USER)
            1 * userService.update(_) >> Promise.value(false)
            assert result.sentResponse == true
            assert result.headers.location.contains("Användare+kunde+inte+uppdateras")
    }

    void 'should create user'() {
        given:
            def formData = [username: "updatedUsername", firstName: "updatedFirstName", lastName: "updatedLastName", emailAddress: "updatedEmailAddress"]
        when:
            def result = requestFixture
                    .uri("user/create")
                    .method("POST")
                    .body(formData.collect { it }.join('&'), APPLICATION_FORM)
                    .handleChain(new WebChain())
        then:
            1 * userService.create(_) >> Promise.value(true)
            assert result.sentResponse == true
            assert result.status.'3xx'
            assert result.headers.location.contains("Användare skapad")
    }

}

package com.ohagner.deviations.handlers

import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.security.AuthenticationService
import groovy.json.JsonOutput
import ratpack.exec.Promise
import ratpack.groovy.test.handling.GroovyRequestFixture
import spock.lang.Specification

class AuthenticationHandlerSpec extends Specification {

    AuthenticationService authenticationService = Mock()

    UserAuthenticationHandler handler = new UserAuthenticationHandler(authenticationService)

    public static final String REQUEST = JsonOutput.toJson([username: "username", password:"password"])

    GroovyRequestFixture requestFixture = GroovyRequestFixture.requestFixture().body(REQUEST, "application/json")

    void 'should authenticate user'() {
        given:
            User expected = new User()
        when:
            def result = requestFixture.handle(handler)
        then:
            1 * authenticationService.authenticate("username", "password") >> Promise.value(expected)
            result.rendered(User) == expected
            result.status.'2xx'
    }

    void 'should fail authentication'() {
        when:
            def result = requestFixture.handle(handler)
        then:
            1 * authenticationService.authenticate("username", "password") >> Promise.value(null)
            result.status.'4xx'
    }

}

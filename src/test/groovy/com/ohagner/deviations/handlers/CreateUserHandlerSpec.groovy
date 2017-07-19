package com.ohagner.deviations.handlers

import com.ohagner.deviations.api.user.endpoint.CreateUserHandler
import com.ohagner.deviations.api.user.domain.Role
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import groovy.json.JsonOutput
import ratpack.exec.Promise
import ratpack.groovy.test.handling.GroovyRequestFixture
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification


class CreateUserHandlerSpec extends Specification {

    UserRepository userRepository = Mock()
    CreateUserHandler createUserHandler = new CreateUserHandler(userRepository)

    GroovyRequestFixture requestFixture

    private static final String VALID_USER = JsonOutput.toJson {
        firstName "firstName"
        lastName "lastName"
        emailAddress "emailAddress"
        credentials {
            username "username"
            password "password"
        }
    }

    def setup() {
        requestFixture = GroovyRequestFixture.requestFixture()
    }

    void 'should create user'() {
        given:
            User expected = User.fromJson(VALID_USER)
            expected.credentials.role = Role.USER
        when:
            def result = requestFixture.body(VALID_USER, "application/json").handle(createUserHandler)
        then:
            1 * userRepository.userExists("username") >> Promise.value(false)
            1 * userRepository.create(_, "password") >> Promise.value(expected)
            result.rendered(User) == expected
            result.status.'2xx'
    }

    void 'should return 400 when user already exists'() {
        when:
            def result = requestFixture.body(VALID_USER, "application/json").handle(createUserHandler)
        then:
            1 * userRepository.userExists("username") >> Promise.value(true)
            0 * userRepository.create(_, "password")
            result.status.code == 400
    }

}

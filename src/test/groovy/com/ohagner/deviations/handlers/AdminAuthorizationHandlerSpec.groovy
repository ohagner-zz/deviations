package com.ohagner.deviations.handlers

import com.ohagner.deviations.api.user.AdminAuthorizationHandler
import com.ohagner.deviations.api.user.Role
import com.ohagner.deviations.config.Constants
import com.ohagner.deviations.api.user.Credentials
import com.ohagner.deviations.api.user.Token
import com.ohagner.deviations.api.user.User
import com.ohagner.deviations.api.user.UserRepository
import ratpack.exec.Promise
import ratpack.jackson.JsonRender
import ratpack.test.handling.RequestFixture
import spock.lang.Specification

import java.time.LocalDate

class AdminAuthorizationHandlerSpec extends Specification {

    public static final String TOKEN = "abcd-1234"
    public static final Token VALID_TOKEN = new Token(value: TOKEN, expirationDate: LocalDate.now(Constants.ZONE_ID).plusWeeks(4))
    public static final Token EXPIRED_TOKEN = new Token(value: TOKEN, expirationDate: LocalDate.now(Constants.ZONE_ID).minusDays(1))
    public static final Credentials VALID_ADMIN_CREDENTIALS = Credentials.builder().apiToken(VALID_TOKEN).role(Role.ADMIN).build()
    public static final Credentials EXPIRED_ADMIN_CREDENTIALS = Credentials.builder().apiToken(EXPIRED_TOKEN).role(Role.ADMIN).build()
    public static final Credentials VALID_USER_CREDENTIALS = Credentials.builder().apiToken(VALID_TOKEN).role(Role.USER).build()
    public static final Credentials EXPIRED_USER_CREDENTIALS = Credentials.builder().apiToken(EXPIRED_TOKEN).role(Role.USER).build()

    UserRepository userRepository
    AdminAuthorizationHandler handler
    RequestFixture requestFixture

    def setup() {
        userRepository = Mock(UserRepository)
        handler = new AdminAuthorizationHandler(userRepository)
        requestFixture = RequestFixture.requestFixture()
                .header(Constants.Headers.USER_TOKEN, TOKEN)
                .registry { r ->
            r.add(UserRepository, userRepository)
        }
    }

    void 'should pass execution on for valid administrator'() {
        when:
            def result = requestFixture.handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(VALID_ADMIN_CREDENTIALS).build())
            result.calledNext
    }

    void 'should fail for administrator with expired token'() {
        when:
            def result = requestFixture.handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(EXPIRED_ADMIN_CREDENTIALS).build())
            result.status.code == 401
            result.rendered(JsonRender).object.message == 'Unauthorized'
    }

    void 'should fail for regular user with valid token'() {
        when:
            def result = requestFixture.handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(VALID_USER_CREDENTIALS).build())
            result.status.code == 401
            result.rendered(JsonRender).object.message == 'Unauthorized'
    }

    void 'should fail for regular user with expired token'() {
        when:
            def result = requestFixture.handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(User.builder()
                    .credentials(EXPIRED_USER_CREDENTIALS).build())
            result.status.code == 401
            result.rendered(JsonRender).object.message == 'Unauthorized'
    }

    void 'should fail when no user is found'() {
        when:
            def result = requestFixture.handle(handler)
        then:
            1 * userRepository.findByApiToken(TOKEN) >> Promise.value(null)
            result.status.code == 401
            result.rendered(JsonRender).object.message == 'Invalid api security token'
    }

    void 'should fail when no apiToken is supplied'() {
        when:
            def result = RequestFixture.requestFixture()
                    .registry { r ->
                r.add(UserRepository, userRepository)
            }.handle(handler)
        then:
            1 * userRepository.findByApiToken(_) >> Promise.value(null)
            result.status.code == 401
            result.rendered(JsonRender).object.message == 'Invalid api security token'
    }

}

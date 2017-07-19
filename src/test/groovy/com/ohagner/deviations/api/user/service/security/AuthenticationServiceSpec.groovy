package com.ohagner.deviations.api.user.service.security

import com.ohagner.deviations.api.user.domain.Role
import com.ohagner.deviations.api.user.domain.Credentials
import com.ohagner.deviations.api.user.domain.User
import com.ohagner.deviations.api.user.repository.UserRepository
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.time.LocalDate

import static com.ohagner.deviations.config.Constants.*

class AuthenticationServiceSpec extends Specification {

    private static final String PASSWORD = "Password"
    private static final String PASSWORD_SALT = HashGenerator.createSalt()

    private User user
    private User adminUser

    def setup() {
        user = User.builder()
                .credentials(new Credentials(username: "username", passwordHash: HashGenerator.generateHash(PASSWORD, PASSWORD_SALT), passwordSalt: PASSWORD_SALT))
                .build()
        adminUser = User.builder()
                .credentials(new Credentials(role: Role.ADMIN, username: "username", passwordHash: HashGenerator.generateHash(PASSWORD, PASSWORD_SALT), passwordSalt: PASSWORD_SALT))
                .build()
    }

    @AutoCleanup
    ExecHarness execHarness = ExecHarness.harness()

    UserRepository userRepository = Mock(UserRepository)
    DefaultAuthenticationService authService = new DefaultAuthenticationService(userRepository)

    void 'should return authenticated user'() {
        when:
            User authenticated = execHarness.yield {
                authService.authenticate(user.credentials.username, PASSWORD)
            }.value
        then:
            1 * userRepository.findByUsername(_) >> Promise.value(user)
            1* userRepository.update(_, _) >> user
            assert authenticated != null
            assert authenticated.credentials.apiToken.value
            assert authenticated.credentials.apiToken.expirationDate == LocalDate.now(ZONE_ID).plusWeeks(4)
    }

    void 'should fail authentication when passwords does not match'() {
        when:
            User authenticated = execHarness.yield {
                authService.authenticate(user.credentials.username, "Invalidpassword")
            }.value
        then:
            1 * userRepository.findByUsername(_) >> Optional.of(user)
            0 * userRepository.update(_, _) >> user
            assert authenticated == null
    }

    void 'should fail authentication'() {
        when:
            User authenticated = execHarness.yield {
                authService.authenticate(user.credentials.username, PASSWORD)
            }.value
        then:
            1 * userRepository.findByUsername(_) >> Promise.value(null)
            0 * userRepository.update(_, _) >> user
            assert authenticated == null
    }

    void 'should return authenticated administrator'() {
        when:
            User authenticated = execHarness.yield {
                authService.authenticateAdministrator(adminUser.credentials.username, PASSWORD)
            }.value
        then:
            1 * userRepository.findByUsername(_) >> Promise.value(adminUser)
            1* userRepository.update(_, _) >> adminUser
            assert authenticated != null
            assert authenticated.credentials.apiToken.value
            assert authenticated.credentials.apiToken.expirationDate == LocalDate.now(ZONE_ID).plusWeeks(4)
    }

    void 'should fail authentication when passwords does not match for administrator'() {
        when:
            User authenticated = execHarness.yield {
                authService.authenticateAdministrator(adminUser.credentials.username, "Invalidpassword")
            }.value
        then:
            1 * userRepository.findByUsername(_) >> Promise.value(adminUser)
            0 * userRepository.update(_, _)
            assert authenticated == null
    }

    void 'should fail authentication when user is not administraor'() {
        when:
            User authenticated = execHarness.yield {
                authService.authenticateAdministrator(user.credentials.username, PASSWORD)
            }.value
        then:
            1 * userRepository.findByUsername(_) >> user
            0 * userRepository.update(_, _)
            assert authenticated == null
    }

}

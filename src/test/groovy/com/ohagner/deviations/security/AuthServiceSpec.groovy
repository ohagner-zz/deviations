package com.ohagner.deviations.security

import com.ohagner.deviations.Role
import com.ohagner.deviations.config.Constants
import com.ohagner.deviations.domain.user.Credentials
import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.repository.UserRepository
import ratpack.test.exec.ExecHarness
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.time.LocalDate

import static com.ohagner.deviations.config.Constants.*

class AuthServiceSpec extends Specification {

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
    AuthService authService = new AuthService(userRepository)

    void 'should return authenticated user'() {
        when:
            User authenticated = execHarness.yield {
                authService.authenticate(user.credentials.username, PASSWORD)
            }.value
        then:
            1 * userRepository.findByUsername(_) >> Optional.of(user)
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
            1 * userRepository.findByUsername(_) >> Optional.empty()
            0 * userRepository.update(_, _) >> user
            assert authenticated == null
    }

    void 'should return authenticated administrator'() {
        when:
            User authenticated = execHarness.yield {
                authService.authenticateAdministrator(adminUser.credentials.username, PASSWORD)
            }.value
        then:
            1 * userRepository.findByUsername(_) >> Optional.of(adminUser)
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
            1 * userRepository.findByUsername(_) >> Optional.of(adminUser)
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

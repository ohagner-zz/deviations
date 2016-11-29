package com.ohagner.deviations.handlers.notification

import com.ohagner.deviations.domain.notification.Notification
import com.ohagner.deviations.domain.user.Credentials
import com.ohagner.deviations.domain.user.User
import com.ohagner.deviations.notifications.NotificationService
import com.ohagner.deviations.repository.UserRepository
import ratpack.groovy.test.handling.GroovyRequestFixture
import spock.lang.Specification
import wslite.rest.RESTClientException

class SendNotificationHandlerSpec extends Specification {

    private static final String USERNAME = "theUsername"

    NotificationService notificationsService
    UserRepository userRepository
    SendNotificationHandler handler

    GroovyRequestFixture requestFixture

    def setup() {
        notificationsService = Mock(NotificationService)
        userRepository = Mock(UserRepository)
        handler = new SendNotificationHandler(userRepository, notificationsService)
        requestFixture = GroovyRequestFixture.requestFixture()
                .registry { registry ->
            registry.add(NotificationService, notificationsService)
            registry.add(UserRepository, userRepository)
        }
    }

    def 'should notify when user exists'() {
        given:
            Notification notification = new Notification(header: "Header", message: "Message")
            User user = new User(credentials:new Credentials(username: USERNAME))
        when:
            def result = requestFixture.handle(handler) {
                pathBinding(["username": USERNAME])
                body(notification.toJson(), "application/json")
            }
        then:
            1 * userRepository.findByUsername(USERNAME) >> Optional.of(user)
            1 * notificationsService.sendNotification(user, _)
            assert result.status.code == 204
            assert result.bodyText == ""
    }

    def 'should respond with 404 when user is missing'() {
        given:
            Notification notification = new Notification(header: "Header", message: "Message")
        when:
            def result = requestFixture.handle(handler) {
                pathBinding(["username": USERNAME])
                body(notification.toJson(), "application/json")
            }
        then:
            1 * userRepository.findByUsername(USERNAME) >> Optional.empty()
            0 * notificationsService.sendNotification(_, _)
            assert result.status.code == 404
    }

    def 'should respond with 500 when notification fails'() {
        given:
            Notification notification = new Notification(header: "Header", message: "Message")
            User user = new User(credentials: new Credentials(username: USERNAME))
        when:
            def result = requestFixture.handle(handler) {
                body(notification.toJson(), "application/json")
                pathBinding(["username": USERNAME])
            }
        then:
            1 * userRepository.findByUsername(USERNAME) >> Optional.of(user)
            notificationsService.sendNotification(user,_) >> { throw new RESTClientException("Connection reset", null, null) }
            assert result.status.code == 500
    }

}

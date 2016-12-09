package com.ohagner.deviations.notifications

import com.ohagner.deviations.api.notification.LogNotifier
import com.ohagner.deviations.api.notification.NotificationType
import spock.lang.Specification

class LogNotifierSpec extends Specification {



    def 'should be applicable'(){
        given:
            LogNotifier notifier = new LogNotifier()
        expect:
            assert notifier.isApplicable([NotificationType.LOG])
    }

    def 'should not be applicable'(){
        given:
            LogNotifier notifier = new LogNotifier()
        expect:
            assert notifier.isApplicable([]) == false
    }

}

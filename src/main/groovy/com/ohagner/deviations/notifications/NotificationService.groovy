package com.ohagner.deviations.notifications

import com.ohagner.deviations.domain.User
import com.ohagner.deviations.domain.Watch
import com.ohagner.deviations.domain.notifications.NotificationType
import com.ohagner.deviations.repository.UserRepository

class NotificationService {

    List<Notifier> notifiers
    UserRepository userRepository

    //TODO: Some sort of injection of all notifiers
    NotificationService(List<Notifier> notifiers, UserRepository userRepository) {
        this.notifiers = notifiers
        this.userRepository = userRepository
    }

    def processNotifications(Watch watch, def deviations) {
        notifiers.each { notifier ->
            if(notifier.isApplicable(watch.notifyBy?: [])) {
                User user = userRepository.findByUsername(watch.username)
                notifier.notify(user, deviations)
            }
        }
    }
}

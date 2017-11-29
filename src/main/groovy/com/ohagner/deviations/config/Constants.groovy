package com.ohagner.deviations.config

import java.time.ZoneId

final class Constants {

    private Constants() {}

    public static final String WATCHES_TO_PROCESS_QUEUE_NAME = "WATCHES_TO_PROCESS"
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris")

    static class Headers {
        public static final String USER_TOKEN = "Authorization"
    }

    static class Session {
        public static final String LOGGED_IN_USER = "loggedInUser"
    }

    static class Admin {
        public static final String USERNAME = "ADMINISTRATOR_USERNAME"
        public static final String PASSWORD = "ADMINISTRATOR_PASSWORD"
    }


    static class Date {
        public static final String LONG_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
        public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd"
        public static final String TIME_FORMAT = "HH:mm"
    }

}

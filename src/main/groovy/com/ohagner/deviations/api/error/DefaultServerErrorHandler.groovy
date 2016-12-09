package com.ohagner.deviations.api.error

import groovy.util.logging.Slf4j
import ratpack.error.ServerErrorHandler
import ratpack.handling.Context

import static ratpack.jackson.Jackson.json

@Slf4j
class DefaultServerErrorHandler implements ServerErrorHandler {

    @Override
    void error(Context context, Throwable throwable) throws Exception {
        log.error("Default error handler caught exception", throwable)
        context.with {
            response.status(500)
            render json([message: "An error occurred"])
        }
    }
}

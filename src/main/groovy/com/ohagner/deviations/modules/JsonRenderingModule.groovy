package com.ohagner.deviations.modules

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton

class JsonRenderingModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    ObjectMapper createMapper() {
        return new ObjectMapper().findAndRegisterModules()
    }
}

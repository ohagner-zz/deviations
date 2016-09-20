package com.ohagner.deviations.json.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import java.time.LocalTime

class LocalTimeSerializer extends StdSerializer<LocalTime> {

    protected LocalTimeSerializer(Class<LocalTime> t) {
        super(t)
    }

    @Override
    void serialize(LocalTime localTime, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString("${localTime.getHour()}:${localTime.getMinute()}")
    }
}

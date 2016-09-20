package com.ohagner.deviations.json.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import java.time.LocalDate


class LocalDateSerializer extends StdSerializer<LocalDate> {

    protected LocalDateSerializer(Class<LocalDate> t) {
        super(t)
    }

    @Override
    void serialize(LocalDate localDate, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString("")
    }
}

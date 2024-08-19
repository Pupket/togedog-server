package pupket.togedogserver.domain.user.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import pupket.togedogserver.domain.user.constant.Time;

import java.io.IOException;

public class TimeDeserializer extends JsonDeserializer<Time> {
    @Override
    public Time deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getText();
        return Time.nameOf(name);
    }
}

package pupket.togedogserver.domain.user.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import pupket.togedogserver.domain.user.constant.Week;

import java.io.IOException;

public class WeekDeserializer extends JsonDeserializer<Week> {
    @Override
    public Week deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getText();
        return Week.nameOf(name);
    }
}

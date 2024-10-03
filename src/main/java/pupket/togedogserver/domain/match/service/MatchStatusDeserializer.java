package pupket.togedogserver.domain.match.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import pupket.togedogserver.domain.match.constant.MatchStatus;

import java.io.IOException;

public class MatchStatusDeserializer extends JsonDeserializer<MatchStatus> {
    @Override
    public MatchStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getText();
        return MatchStatus.nameOf(name);
    }
}

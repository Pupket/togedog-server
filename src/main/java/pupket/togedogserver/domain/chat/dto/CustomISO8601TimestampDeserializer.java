package pupket.togedogserver.domain.chat.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

public class CustomISO8601TimestampDeserializer extends JsonDeserializer<Timestamp> {

    @Override
    public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        // ISO 8601 형식의 문자열을 Timestamp로 변환
        return Timestamp.from(Instant.parse(p.getText()));
    }
}
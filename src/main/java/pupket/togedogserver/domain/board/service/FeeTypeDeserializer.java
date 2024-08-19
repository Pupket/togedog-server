package pupket.togedogserver.domain.board.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import pupket.togedogserver.domain.board.constant.FeeType;

import java.io.IOException;

public class FeeTypeDeserializer extends JsonDeserializer<FeeType> {
    @Override
    public FeeType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getText();
        return FeeType.nameOf(name);
    }
}

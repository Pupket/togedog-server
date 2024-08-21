package pupket.togedogserver.domain.dog.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import pupket.togedogserver.domain.dog.constant.DogType;

import java.io.IOException;

public class DogTypeDeserializer extends JsonDeserializer<DogType> {
    @Override
    public DogType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getText();
        return DogType.nameOf(name);
    }
}

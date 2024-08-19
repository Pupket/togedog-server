package pupket.togedogserver.domain.user.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import pupket.togedogserver.domain.user.constant.UserGender;

import java.io.IOException;

public class UserGenderDeserializer extends JsonDeserializer<UserGender> {
    @Override
    public UserGender deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getText();
        return UserGender.nameOf(name);
    }
}

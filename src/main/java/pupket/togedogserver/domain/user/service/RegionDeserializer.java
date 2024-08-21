package pupket.togedogserver.domain.user.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import pupket.togedogserver.domain.user.constant.Region;

import java.io.IOException;

public class RegionDeserializer extends JsonDeserializer<Region> {
    @Override
    public Region deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getText();
        return Region.nameOf(name);
    }
}

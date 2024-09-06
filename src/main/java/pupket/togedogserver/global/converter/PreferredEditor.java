package pupket.togedogserver.global.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import pupket.togedogserver.domain.user.dto.request.Preferred;

import java.beans.PropertyEditorSupport;

public class PreferredEditor extends PropertyEditorSupport {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            Preferred preferred = objectMapper.readValue(text, Preferred.class);
            setValue(preferred);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid value for Preferred: " + text, e);
        }
    }
}
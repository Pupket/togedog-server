package pupket.togedogserver.global.converter;

import java.beans.PropertyEditorSupport;

public class CustomEnumEditor<T extends Enum<T>> extends PropertyEditorSupport {
    private final Class<T> type;

    public CustomEnumEditor(Class<T> type) {
        this.type = type;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            // 빈 값이 들어오면 null로 설정
            setValue(null);
        } else {
            try {
                // Enum 값으로 변환
                T result = Enum.valueOf(type, text.toUpperCase());
                setValue(result);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid value for enum: " + text, e);
            }
        }
    }

    @Override
    public String getAsText() {
        T result = (T) getValue();
        return result != null ? result.name() : "";
    }
}
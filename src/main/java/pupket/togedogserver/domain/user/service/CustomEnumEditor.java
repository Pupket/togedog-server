package pupket.togedogserver.domain.user.service;

import java.beans.PropertyEditorSupport;

public class CustomEnumEditor<T extends Enum<T>> extends PropertyEditorSupport {
    private final Class<T> type;

    public CustomEnumEditor(Class<T> type) {
        this.type = type;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        T result = Enum.valueOf(type, text.toUpperCase()); // 변환 로직 수정 필요
        setValue(result);
    }

    @Override
    public String getAsText() {
        T result = (T) getValue();
        return result != null ? result.name() : "";
    }
}
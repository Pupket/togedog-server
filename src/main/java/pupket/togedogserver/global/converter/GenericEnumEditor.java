package pupket.togedogserver.global.converter;

import java.beans.PropertyEditorSupport;

public class GenericEnumEditor<T extends Enum<T>> extends PropertyEditorSupport {
    private final Class<T> enumType;
    private final StringToEnumConverter<T> converter;

    public GenericEnumEditor(Class<T> enumType, StringToEnumConverter<T> converter) {
        this.enumType = enumType;
        this.converter = converter;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(converter.convert(text));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid value for enum: " + text, ex);
        }
    }

    @Override
    public String getAsText() {
        T value = (T) getValue();
        return value != null ? value.name() : "";
    }

    @FunctionalInterface
    public interface StringToEnumConverter<T extends Enum<T>> {
        T convert(String text) throws IllegalArgumentException;
    }
}

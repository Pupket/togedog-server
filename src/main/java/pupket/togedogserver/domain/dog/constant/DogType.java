package pupket.togedogserver.domain.dog.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pupket.togedogserver.domain.dog.service.DogTypeDeserializer;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.DogException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = DogTypeDeserializer.class)
public enum DogType {
    SMALL("소형견"), MID("중형견"), BIG("대형견"), SUPER("초대형견");

    final private String breed;

    DogType(String breed) {
        this.breed = breed;
    }

    @JsonValue
    public String getBreed() {
        return breed;
    }

    public static DogType nameOf(String name) {
        for (DogType data : DogType.values()) {
            if (data.getBreed().equals(name)) {
                return data;
            }
        }
        throw new DogException(ExceptionCode.INVALID_ENUM_PARAMETER);
    }




}

package pupket.togedogserver.domain.dog.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pupket.togedogserver.domain.dog.service.BreedDeserializer;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.DogException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = BreedDeserializer.class)
public enum Breed {
    SMALL("소형견"), MID("중형견"), BIG("대형견"), SUPER("초대형견");

    final private String breed;

    Breed(String breed) {
        this.breed = breed;
    }

    @JsonValue
    public String getBreed() {
        return breed;
    }

    public static Breed nameOf(String name) {
        for (Breed data : Breed.values()) {
            if (data.getBreed().equals(name)) {
                return data;
            }
        }
        throw new DogException(ExceptionCode.INVALID_ENUM_PARAMETER);
    }




}

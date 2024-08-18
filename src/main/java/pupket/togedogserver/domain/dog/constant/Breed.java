package pupket.togedogserver.domain.dog.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Breed {
    SMALL("소형견"), MID("중형견"), BIG("대형견"), SUPER("초대형견");

    final private String breed;

    Breed(String breed) {
        this.breed = breed;
    }

    public String getBreed() {
        return breed;
    }




}

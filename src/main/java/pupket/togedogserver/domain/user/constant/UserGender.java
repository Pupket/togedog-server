package pupket.togedogserver.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserGender {
    MALE("남성"), FEMALE("여성");

    private final String gender;
    UserGender(String gender) {
        this.gender = gender;
    }
    public String getGender() {
        return gender;
    }

    public static UserGender nameOf(String name) {
        for (UserGender data : UserGender.values()) {
            if (data.getGender().equals(name)) {
                return data;
            }
        }
        return null;
    }
}

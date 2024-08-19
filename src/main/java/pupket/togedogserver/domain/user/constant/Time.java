package pupket.togedogserver.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pupket.togedogserver.domain.user.service.TimeDeserializer;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = TimeDeserializer.class)
public enum Time {
    MORNING("아침"), LUNCH("점심"), AFTERNOON("오후"), EVENING("저녁"), DAWN("새벽");

    private final String time;

    Time(String time) {
        this.time = time;
    }

    @JsonValue
    public String getTime() {
        return time;
    }

    public static Time nameOf(String name) {
        for (Time data : Time.values()) {
            if (data.getTime().equals(name)) {
                return data;
            }
        }
        throw new MemberException(ExceptionCode.INVALID_ENUM_PARAMETER);
    }
}

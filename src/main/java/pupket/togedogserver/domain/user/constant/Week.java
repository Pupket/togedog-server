package pupket.togedogserver.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pupket.togedogserver.domain.user.service.WeekDeserializer;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = WeekDeserializer.class)
public enum Week {
    MON("월요일"), TUE("화요일"), WED("수요일"), THU("목요일"), FRI("금요일"), SAT("토요일"), SUN("일요일");

    private final String week;
    Week(final String week) {
        this.week = week;
    }

    @JsonValue
    public String getWeek() {
        return week;
    }

    public static Week nameOf(String name) {
        for (Week data : Week.values()) {
            if (data.getWeek().equals(name)) {
                return data;
            }
        }
        throw new MemberException(ExceptionCode.INVALID_ENUM_PARAMETER);
    }
}

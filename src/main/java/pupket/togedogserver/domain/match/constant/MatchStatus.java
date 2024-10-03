package pupket.togedogserver.domain.match.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pupket.togedogserver.domain.match.service.MatchStatusDeserializer;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = MatchStatusDeserializer.class)
public enum MatchStatus {
    MATCHED("매칭 성사"), UNMATCHED("매칭 미성사");

    private final String status;
    MatchStatus(String status) {
        this.status = status;
    }

    public static MatchStatus nameOf(String name) {
        for (MatchStatus data : MatchStatus.values()) {
            if (data.getStatus().equals(name)) {
                return data;
            }
        }
        throw new MemberException(ExceptionCode.INVALID_ENUM_PARAMETER);
    }

    @JsonValue
    public String getStatus() {
        return status;
    }

}

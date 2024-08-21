package pupket.togedogserver.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AccountStatus {
    ACTIVE("활성화"), DOMANT("휴면"), DELETED("삭제");

    private final String status;
    AccountStatus(final String status) {
        this.status = status;
    }

    @JsonValue
    public String getStatus() {
        return status;
    }

    public static AccountStatus nameOf(String name) {
        for (AccountStatus data : AccountStatus.values()) {
            if (data.getStatus().equals(name)) {
                return data;
            }
        }
        throw new MemberException(ExceptionCode.INVALID_ENUM_PARAMETER);
    }
}

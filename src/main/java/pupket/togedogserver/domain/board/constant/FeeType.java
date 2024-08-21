package pupket.togedogserver.domain.board.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pupket.togedogserver.domain.board.service.FeeTypeDeserializer;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.BoardException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = FeeTypeDeserializer.class)
public enum FeeType {
    PER_HOUR("시급"), PER_CASE("건별");

    private final String feeType;

    FeeType(String feeType) {
        this.feeType = feeType;
    }

    @JsonValue
    public String getFeeType() {
        return this.feeType;
    }

    public static FeeType nameOf(String name) {
        for (FeeType data : FeeType.values()) {
            if (data.getFeeType().equals(name)) {
                return data;
            }
        }
        throw new BoardException(ExceptionCode.INVALID_ENUM_PARAMETER);
    }
}

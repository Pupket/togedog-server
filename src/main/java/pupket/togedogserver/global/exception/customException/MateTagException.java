package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class MateTagException extends TogedogException {
    public MateTagException(ExceptionCode errorCode) {
        super(errorCode);
    }
}

package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class MateException extends TogedogException {
    public MateException(ExceptionCode errorCode) {
        super(errorCode);
    }
}
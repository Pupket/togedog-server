package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class OwnerException extends TogedogException {
    public OwnerException(ExceptionCode errorCode) {
        super(errorCode);
    }
}

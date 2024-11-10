package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class FcmException extends TogedogException {
    public FcmException(ExceptionCode errorCode) {
        super(errorCode);
    }
    }

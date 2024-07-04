package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class MatchingException extends TogedogException {
    public MatchingException(ExceptionCode errorCode) {
        super(errorCode);
    }

}

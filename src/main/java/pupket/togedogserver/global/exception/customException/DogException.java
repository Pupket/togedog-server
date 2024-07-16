package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class DogException extends TogedogException {
    public DogException(ExceptionCode errorCode) {
        super(errorCode);
    }

}

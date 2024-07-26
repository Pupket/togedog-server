package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class WalkingPlaceTagException extends TogedogException {
    public WalkingPlaceTagException(ExceptionCode errorCode) {
        super(errorCode);
    }
}

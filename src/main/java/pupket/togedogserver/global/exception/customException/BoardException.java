package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class BoardException extends TogedogException {
    public BoardException(ExceptionCode errorCode) {
        super(errorCode);
    }
}

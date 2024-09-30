package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class BoardDogException extends TogedogException {
    public BoardDogException(ExceptionCode errorCode) {
        super(errorCode);
    }
    }

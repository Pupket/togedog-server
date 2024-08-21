package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class ChatException extends TogedogException {
    public ChatException(ExceptionCode errorCode) {
        super(errorCode);
    }
}

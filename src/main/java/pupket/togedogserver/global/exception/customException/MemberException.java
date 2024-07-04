package pupket.togedogserver.global.exception.customException;


import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class MemberException extends TogedogException {
    public MemberException(ExceptionCode errorCode) {
        super(errorCode);
    }
}

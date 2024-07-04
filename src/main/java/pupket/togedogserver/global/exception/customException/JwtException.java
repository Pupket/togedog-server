package pupket.togedogserver.global.exception.customException;


import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class JwtException extends TogedogException {
    public JwtException(ExceptionCode errorCode) {
        super(errorCode);
    }

    public JwtException() {
        super(ExceptionCode.INVALID_PARAMETER);
    }
}

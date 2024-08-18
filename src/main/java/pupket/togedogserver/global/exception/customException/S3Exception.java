package pupket.togedogserver.global.exception.customException;

import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.TogedogException;

public class S3Exception extends TogedogException {
    public S3Exception(ExceptionCode errorCode) {
        super(errorCode);
    }
}


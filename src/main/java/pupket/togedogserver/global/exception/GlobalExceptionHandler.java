package pupket.togedogserver.global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pupket.togedogserver.global.exception.customException.JwtException;
import pupket.togedogserver.global.exception.customException.MemberException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger defaultLogger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    Logger exceptionLogger = LoggerFactory.getLogger("ExceptionLogger");

    @ExceptionHandler(TogedogException.class)
    public ResponseEntity<ExceptionResponse> handleAlleException(TogedogException ex) {
        defaultLogger.warn(ex.getMessage());
        exceptionLogger.warn(ex.getMessage(), ex);

        ExceptionResponse exceptionResponse = ExceptionResponse.fromException(ex.getExceptionCode());

        return ResponseEntity.status(exceptionResponse.httpStatus()).body(exceptionResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResponse> handleDefaultException(Exception ex) {
        defaultLogger.error(ex.getMessage(), ex);
        exceptionLogger.error(ex.getMessage(), ex);

        ExceptionResponse exceptionResponse = ExceptionResponse.fromError(ex);
        return ResponseEntity.status(exceptionResponse.httpStatus()).body(exceptionResponse);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ExceptionResponse> handleJwtException(JwtException ex) {
        defaultLogger.error(ex.getMessage(), ex);
        exceptionLogger.error(ex.getMessage(), ex);

        ExceptionResponse exceptionResponse = ExceptionResponse.fromException(ex.getExceptionCode());
        return ResponseEntity.status(exceptionResponse.httpStatus()).body(exceptionResponse);
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ExceptionResponse> handleMemberException(MemberException ex) {
        defaultLogger.error("error message={}", ex.getMessage(), ex);
        exceptionLogger.error("exception message={}",ex.getMessage(), ex);
        ExceptionResponse exceptionResponse = ExceptionResponse.fromException(ex.getExceptionCode());
        return ResponseEntity.status(exceptionResponse.httpStatus()).body(exceptionResponse);
    }
}

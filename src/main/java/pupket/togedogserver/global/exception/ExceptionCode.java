package pupket.togedogserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    // 500
    NOT_HANDLED_EXCEPTION(INTERNAL_SERVER_ERROR, "Unhandled exception occurred.", 500),
    REDIS_CONNECTION_FAILURE(INTERNAL_SERVER_ERROR, "Failed to connect to Redis or Failed to save Record", 500),
    EMAIL_SEND_FAILURE(INTERNAL_SERVER_ERROR, "Failed to send email.", 500),
    UNEXPECTED_ERROR(INTERNAL_SERVER_ERROR, "An unexpected error occurred.", 500),

    // 400
    PASSWORD_MISMATCH(BAD_REQUEST, "Passwords do not match.", 400),
    INVALID_CURRENT_PASSWORD(BAD_REQUEST, "The current password is incorrect.", 400),
    INVALID_AUTH_CODE(BAD_REQUEST, "Invalid email authentication code.", 400),
    NOT_FOUND_REFRESH_TOKEN_IN_COOKIE(BAD_REQUEST, "Refresh token not found in cookie.", 400),
    INVALID_PARAMETER(BAD_REQUEST, "Invalid request parameter.", 400),
    INVALID_FILE_EXTENTION(BAD_REQUEST, "Invalid File Extention", 400),
    INVALID_ENUM_PARAMETER(BAD_REQUEST, "Invalid Enum Parameter", 400),


    // 401
    UNAUTHORIZED_LOGIN(UNAUTHORIZED, "Login failed: authentication failed.", 401),
    TOKEN_EXPIRED(UNAUTHORIZED, "Token has expired.", 401),
    INVALID_TOKEN(UNAUTHORIZED, "Invalid token provided.", 401),
    UNSUPPORTED_TOKEN(UNAUTHORIZED, "Token format is unsupported.", 401),
    NOT_FOUND_TOKEN(UNAUTHORIZED, "Token not found.", 401),
    NOT_FOUND_REFRESH_TOKEN(UNAUTHORIZED, "Refresh token not found for the user.", 401),
    INVALID_AUTHENTICATION(UNAUTHORIZED, "Invalid authentication.", 401),
    MALFORMED_TOKEN(UNAUTHORIZED, "Malformed token.", 401),
    EXPIRED_TOKEN(UNAUTHORIZED, "Expired token.", 401),
    // 409
    DUPLICATE_RESOURCE(CONFLICT, "Resource already exists.", 409),
    NOT_FOUND_MEMBER(CONFLICT, "Member not found.", 409),
    NOT_FOUND_OWNER(CONFLICT, "Owner not found.", 409),
    NOT_FOUND_MATE(CONFLICT, "Mate not found.", 409),
    NOT_FOUND_BOARD(CONFLICT, "Board not found.", 409),
    NOT_FOUND_MATCH(CONFLICT, "Match not found.", 409),
    NOT_FOUND_CHATROOM(CONFLICT, "Room not found.", 409),
    MEMBER_ALREADY_EXISTS(CONFLICT, "Member already exists.", 409),
    MATE_ALREADY_EXIST(CONFLICT, "MATE already exists", 409),
    NICKNAME_ALREADY_EXISTS(CONFLICT, "Nickname already exists.", 409),
    NOT_MATCHED_WRITER(CONFLICT, "Writer does not match.", 409),
    INVALID_BOARD(CONFLICT, "Invalid board.", 409),
    MEMBER_ALREADY_WITHDRAW(CONFLICT, "Member has already withdrawn.", 409),
    NOT_FOUND_BOARDCOMMENT(CONFLICT, "Board comment not found.", 409),
    NOT_FOUND_DOG(CONFLICT, "Dog not found", 409),
    NOT_FOUND_MATE_TAG(CONFLICT, "Dog not Mate Tag", 409),
    DOG_ALREADY_EXISTS(CONFLICT, "Dog already exists.", 409),
    AVAILABLE_FOR_REGISTRATION_EXCEEDED(CONFLICT, "Available for registration exceeded(Max=5)", 409),
    NOT_FOUND_WALKING_PLACE_TAG(CONFLICT, "WalkingPlaceTag not found", 409),
    FILE_IS_EMPTY(CONFLICT, "File is Empty", 409),
    IO_EXCEPTION_ON_IMAGE_UPLOAD(CONFLICT, "IO Exception happened on Image Upload", 409),
    NO_FILE_EXTENTION(CONFLICT, "No file Extention", 409),
    PUT_OBJECT_EXCEPTION(CONFLICT, "Put Object Exception", 409),
    IO_EXCEPTION_ON_IMAGE_DELETE(CONFLICT, "IO Exception on Image Delete", 409),
    YOUR_OWN_NICKNAME(CONFLICT, "Parameter is your own nickname", 409),
    ALREADY_ACCEPTED(CONFLICT, "Matching Already Accepted, Somebody Accepted", 409),
    ALREADY_REJECTED(CONFLICT, "MATCHING ALREADY ACCEPTED", 409),
    ALREADY_MATCHED(CONFLICT, "Already Matched", 409),
    NOT_YOUR_DOG(CONFLICT, "Not your Dog, Check your Dog Id", 409),
    ACCEPT_SHOULD_TRY_RECIEVER(CONFLICT, "Board Writer Can't Accept", 409),
    NOT_FOUND_BOARDDOG(CONFLICT, "Can't find BoardDog", 409),
    ALREADY_COMPLETED(CONFLICT, "Matching Already Completed", 409),
    MATE_NOT_REGIST(CONFLICT, "Mate Not Register", 409),
    DUPLICATE_LOGIN(CONFLICT, "Duplicate Login", 409),
    INVALID_TIME_FORMAT(CONFLICT, "Invalid Time Format", 409),
    INTERRUPTION_OR_EXECUTION_ERR(CONFLICT, "Interruption or Execution Error", 409),
    NOT_FOUND_SCHEDULE(CONFLICT, "Not Found Schedule", 409 ),
    NOT_FOUND_FCM_TOKEN(CONFLICT, "Not Found FCM Token", 409 ), 
    NOT_FOUND_ACCESS_TOKEN(CONFLICT, "Not Found Access Token", 409 ),
    FCM_INITIALIZATION_ERROR(CONFLICT, "FCM Initialization Error" , 409 )
    , NO_FILE_EXTENSION(CONFLICT, "No file Extention" , 409 ),
    INVALID_FILE_EXTENSION(CONFLICT, "Invalid file Extention" , 409 )
    , SCHDULE_CONFICT(CONFLICT,"Schedule is duplacated" ,409 );


    private final HttpStatus httpStatus;
    private final String message;
    private final Integer code;
}

package pupket.togedogserver.domain.match.constant;

import lombok.Getter;

@Getter
public enum CompleteStatus {
    COMPLETE("산책 완료"), INCOMPLETE("산책 전");

    private final String status;

    CompleteStatus(String status) {
        this.status = status;
    }
}

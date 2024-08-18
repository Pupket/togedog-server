package pupket.togedogserver.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AccountStatus {
    ACTIVE("활성화"), DOMANT("휴면"), DELETED("삭제");

    private final String status;
    AccountStatus(final String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public static AccountStatus nameOf(String name) {
        for (AccountStatus data : AccountStatus.values()) {
            if (data.getStatus().equals(name)) {
                return data;
            }
        }
        return null;
    }
}

package pupket.togedogserver.domain.match.constant;

public enum CompleteStatus {
    COMPLETE("활성화"), INCOMPLETE("비활성화");

    private String status;

    CompleteStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
}

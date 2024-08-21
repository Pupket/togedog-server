package pupket.togedogserver.domain.match.constant;

public enum MatchStatus {
    MATCHED("매칭 성사"), UNMATCHED("매칭 미성사");

    private final String status;
    MatchStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
}

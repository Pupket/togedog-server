package pupket.togedogserver.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Time {
    MORNING("아침"), LUNCH("점심"), AFTERNOON("오후"), EVENING("저녁"), DAWN("새벽");

    private final String time;

    Time(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public static Time nameOf(String name) {
        for (Time data : Time.values()) {
            if (data.getTime().equals(name)) {
                return data;
            }
        }
        return null;
    }
}

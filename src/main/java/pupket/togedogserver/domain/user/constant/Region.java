package pupket.togedogserver.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pupket.togedogserver.domain.user.service.RegionDeserializer;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = RegionDeserializer.class)
public enum Region {
    SEOUL("서울"), INCHEON("인천"), GYEONGGI("경기"), CHUNGCHEONG("충청"), GYEONGSANG("경상"), JEOLLA("전라"), GANGWON("강원"), JEJU("제주");

    private final String region;

    Region(final String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public static Region nameOf(String name) {
        for (Region data : Region.values()) {
            if (data.getRegion().equals(name)) {
                return data;
            }
        }
        return null;
    }

}

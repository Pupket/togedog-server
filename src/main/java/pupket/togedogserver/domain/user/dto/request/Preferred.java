package pupket.togedogserver.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.Time;
import pupket.togedogserver.domain.user.constant.Week;

import java.util.Set;

@Data
@Schema(description = "선호하는 상세 정보")
public class Preferred {

    @Schema(description = "선호하는 요일(월요일~일요일)", example = "[\"월요일\",\"화요일\",\"수요일\"]", required = true)
    private Set<Week> weeks;

    @Schema(description = "선호하는 시간대(아침, 점심, 오후, 저녁, 새벽)", example = "[\"아침\",\"저녁\"]", required = true)
    private Set<Time> times;

    @Schema(description = "선호하는 스타일", example = "[\"귀여운\"]", required = true)
    private Set<String> hashTag;

    @Schema(description = "선호하는 견종(소형견, 중형견, 대형견, 초대형견)", example = "[\"중형견\", \"대형견\"]", required = true)
    private Set<DogType> dogTypes;

    @Schema(description = "선호하는 지역", example = "경상", required = true)
    private Region region;

}

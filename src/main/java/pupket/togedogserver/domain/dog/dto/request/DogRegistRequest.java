package pupket.togedogserver.domain.dog.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import pupket.togedogserver.domain.user.constant.Region;

import java.util.Set;

@Data
@Schema(description = "강아지 등록 요청")
public class DogRegistRequest {

    @Schema(description = "강아지 이름", example = "Buddy", required = true)
    private String name;

    @Schema(description = "강아지 품종", example = "Golden Retriever", required = true)
    private String breed;

    @Schema(description = "중성화 여부", example = "true", required = true)
    private boolean neutered;

    @Schema(description = "강아지 성별 (true: 남, false: 여)", example = "true", required = true)
    private boolean dogGender;

    @Schema(description = "강아지 체중", example = "30", required = true)
    private int weight;

    @Schema(description = "지역 (SEOUL, INCHEON, GYEONGGI, CHUNGCHEONG, GYEONGSANG, JEOLLA, GANGWON, JEJU)", example = "SEOUL", required = true)
    private Region region;

    @Schema(description = "비고", example = "귀여운 강아지입니다. 슬개골이 약해요", required = false)
    private String notes;

    @Schema(description = "태그", example = "[\"친근한\", \"활발한\"]", required = false)
    private Set<String> tags;

    @Schema(description = "백신접종 유무", example = "true", required = true)
    private boolean vaccine;

    @Schema(description = "나이", example = "21", required = true)
    private int age;
}

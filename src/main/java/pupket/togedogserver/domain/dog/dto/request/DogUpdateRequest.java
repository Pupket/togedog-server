package pupket.togedogserver.domain.dog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "강아지 수정 요청")
public class DogUpdateRequest {

    @Schema(description = "강아지 프로필 ID", example = "1")
    private Long id;

    @Schema(description = "강아지 이름", example = "Buddy")
    private String name;

    @Schema(description = "강아지 품종", example = "아프간 하운드")
    private String breed;

    @Schema(description = "중성화 여부", example = "true")
    private boolean neutered;

    @Schema(description = "강아지 성별 (true: 남, false: 여)", example = "true")
    private boolean dogGender;

    @Schema(description = "강아지 체중", example = "30")
    private int weight;

    @Schema(description = "지역 (서울, 인천, 경기, 충청, 경상, 전라, 강원, 제주)", example = "강원")
    private String region;

    @Schema(description = "비고", example = "Very friendly")
    private String notes;

    @Schema(description = "태그", example = "[\"friendly\", \"energetic\"]")
    private Set<String> tags;

    @Schema(description = "백신접종 유무", example = "true")
    private boolean vaccine;

    @Schema(description = "나이", example = "21")
    private int age;

}

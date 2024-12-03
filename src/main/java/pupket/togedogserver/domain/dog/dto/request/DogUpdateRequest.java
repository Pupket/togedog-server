package pupket.togedogserver.domain.dog.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "강아지 수정 요청")
public class DogUpdateRequest {

    @JsonProperty("id")
    @Schema(description = "강아지 프로필 ID", example = "1")
    private Long id;

    @JsonProperty("name")
    @Schema(description = "강아지 이름", example = "Buddy")
    private String name;

    @JsonProperty("breed")
    @Schema(description = "강아지 품종", example = "아프간 하운드")
    private String breed;

    @JsonProperty("neutered")
    @Schema(description = "중성화 여부", example = "true")
    private boolean neutered;

    @JsonProperty("dogGender")
    @Schema(description = "강아지 성별 (true: 남, false: 여)", example = "true")
    private boolean dogGender;

    @JsonProperty("weight")
    @Schema(description = "강아지 체중", example = "30")
    private int weight;

    @JsonProperty("region")
    @Schema(description = "지역 (서울, 인천, 경기, 충청, 경상, 전라, 강원, 제주)", example = "강원")
    private String region;

    @JsonProperty("notes")
    @Schema(description = "비고", example = "Very friendly")
    private String notes;

    @JsonProperty("tags")
    @Schema(description = "태그", example = "[\"friendly\", \"energetic\"]")
    private Set<String> tags;

    @JsonProperty("vaccine")
    @Schema(description = "백신접종 유무", example = "true")
    private boolean vaccine;

    @JsonProperty("age")
    @Schema(description = "나이", example = "21")
    private int age;
}
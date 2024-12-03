package pupket.togedogserver.domain.dog.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pupket.togedogserver.domain.user.constant.Region;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "강아지 등록 요청")
public class DogRegistRequest {

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
    @Schema(description = "지역 (서울, 인천, 경기, 충청, 경상, 전라, 강원, 제주)", example = "서울")
    private Region region;

    @JsonProperty("notes")
    @Schema(description = "비고", example = "귀여운 강아지입니다. 슬개골이 약해요")
    private String notes;

    @JsonProperty("tags")
    @Schema(description = "태그", example = "[\"친근한\", \"활발한\"]")
    private Set<String> tags;

    @JsonProperty("vaccine")
    @Schema(description = "백신접종 유무(true:접종, flase:미접종)", example = "true")
    private boolean vaccine;

    @JsonProperty("age")
    @Schema(description = "나이", example = "21")
    private int age;
}

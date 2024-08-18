package pupket.togedogserver.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.user.constant.*;

import java.util.Set;

@Data
@Schema(description = "산책 메이트 프로필 수정 요청")
public class UpdateMateRequest {

    @Schema(description = "닉네임", example = "sunro1234", required = true)
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 10, message = "닉네임은 10자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[\\p{L}0-9]+$", message = "닉네임에는 특수 문자를 사용할 수 없습니다.")
    private String nickname;

    @Schema(description = "사용자 성별", example = "MALE", required = true)
    private UserGender userGender;

    @Schema(description = "성별 공개 여부", example = "ACTIVE", required = true)
    private Visibility genderVisibility;

    @Schema(description = "전화번호", example = "12341234", required = true)
    @NotBlank(message = "연락처는 필수 입력 값입니다.")
    @Pattern(regexp = "^[0-9]+$", message = "연락처에는 특수 문자, 공백을 사용할 수 없습니다.")
    private String phoneNumber;

    @Schema(description = "수용 가능한 강아지 수", example = "12", required = true)
    @Min(value = 1, message = "수용 가능한 강아지는 한 마리 이상이어야 합니다.")
    private int accommodatableDogsCount;

    @Schema(description = "경력", example = "능수능란", required = true)
    @Size(max = 500, message = "경력은 500자 이하로 입력해야 합니다.")
    private String career;

    @Schema(description = "선호하는 견종", example = "[\"MID\", \"SUPER\"]", required = true)
    private Set<Breed> preferredBreed;

    @Schema(description = "선호하는 스타일", example = "[\"귀여운\"]", required = true)
    private Set<String> preferredStyle;

    @Schema(description = "선호하는 요일", example = "[\"MON\", \"TUE\", \"WED\"]", required = true)
    private Set<Week> preferredWeek;

    @Schema(description = "선호하는 시간대", example = "[\"MORNING\", \"EVENING\"]", required = true)
    private Set<Time> preferredTime;

    @Schema(description = "지역", example = "JEJU", required = true)
    private Region region;
}
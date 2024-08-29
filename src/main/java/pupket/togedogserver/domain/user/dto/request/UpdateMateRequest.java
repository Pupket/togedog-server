package pupket.togedogserver.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Visibility;

@Data
@Schema(description = "산책 메이트 프로필 수정 요청")
public class UpdateMateRequest {

    @Schema(description = "닉네임", example = "sunro1234", required = true)
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 10, message = "닉네임은 10자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[\\p{L}0-9]+$", message = "닉네임에는 특수 문자를 사용할 수 없습니다.")
    private String nickname;

    @Schema(description = "남성,여성", example = "여성", required = true)
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

    @Schema(description = "선호하는 상세 정보", required = true)
    private Preferred preferredDetails;

    @Schema(description = "지역(서울, 인천, 경기, 충청, 경상, 전라, 강원, 제주)", example = "제주", required = true)
    private Region region;

    @Schema(description = "지역(서울, 인천, 경기, 충청, 경상, 전라, 강원, 제주)", example = "경상", required = true)
    private Region preferredRegion;
}

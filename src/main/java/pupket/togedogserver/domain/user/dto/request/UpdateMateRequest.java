package pupket.togedogserver.domain.user.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateMateRequest {


    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 10, message = "닉네임은 10자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[\\p{L}0-9]+$", message = "닉네임에는 특수 문자를 사용할 수 없습니다.")
    private String nickname;

    private String profileImage;

    @NotBlank(message = "성별은 필수 입력 값입니다.")
    private String userGender;

    @NotBlank(message = "성별 표시 유무는 필수 입력 값입니다.")
    private String genderVisibility;

    @NotBlank(message = "연락처는 필수 입력 값입니다.")
    @Pattern(regexp = "^[0-9]+$", message = "연락처에는 특수 문자, 공백을 사용할 수 없습니다.")
    private String phoneNumber;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    private int age;

    @Min(value = 1, message = "수용가능한 강아지는 한마리 이상이여야 합니다.")
    private int accommodatableDogsCount;

    @Size(max = 500, message = "경력은 500자 이하로 입력해야 합니다.")
    private String career;

    private List<String> preferredBreed;

    private List<String> preferredStyle;

    private List<String> preferredWeek;

    private List<String> preferredTime;

    private String region;
}

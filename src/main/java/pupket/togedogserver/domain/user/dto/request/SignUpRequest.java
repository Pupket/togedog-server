package pupket.togedogserver.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class SignUpRequest {

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 8, message = "닉네임은 8자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[\\p{L}0-9]+$", message = "닉네임에는 특수 문자를 사용할 수 없습니다.")
    private String nickname;

//    private String profileImage;

    @NotBlank(message = "성별은 필수 입력 값입니다.")
    private String userGender;

    @NotBlank(message = "성별 표시 유무는 필수 입력 값입니다.")
    private String genderVisibility;

    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String birthDay;


}

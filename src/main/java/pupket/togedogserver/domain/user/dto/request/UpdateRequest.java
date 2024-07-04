package pupket.togedogserver.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRequest {


    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 10, message = "닉네임은 10자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[\\p{L}0-9]+$", message = "닉네임에는 특수 문자를 사용할 수 없습니다.")
    private String nickName;

    @NotBlank(message = "성별 표시 유무는 필수 입력 값입니다.")
    private String genderVisibility;

//    private String profileImage;

    private String address1;

    private String address2;

    private Double mapX;
    private Double mapY;
}

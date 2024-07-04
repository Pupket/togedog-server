package pupket.togedogserver.domain.user.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FindUserResponse {

    private String uuid;
    private String nickname;
    private String userGender;
    private LocalDate birthDay;
    private String profileImage;
    private String genderVisibility;
    private String address1;
    private String address2;
}

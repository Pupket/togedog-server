package pupket.togedogserver.domain.user.dto.response;

import lombok.Data;

@Data
public class FindUserResponse {

    private String uuid;
    private String nickname;
    private String userGender;
    private String birthday;
    private String birthyear;
    private String profileImage;
    private String genderVisibility;
    private String address1;
    private String address2;
}

package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.entity.User;

@Data
@Builder
public class FindUserInfoResponse {

    private Long uuid;
    private String email;
    private String name;
    private String platform;
    private String phoneNumber;

    public static FindUserInfoResponse from(User user) {
        return FindUserInfoResponse.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .name(user.getName())
                .platform(RoleType.toKoreanValue(user.getRole()))
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}

package pupket.togedogserver.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.dto.request.SignUpRequest;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.domain.user.entity.User;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface UserMapper {


    //SignUpRequest - > User
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "mate", ignore = true)
    @Mapping(target = "dog", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "chatting", ignore = true)
    @Mapping(source = "userGender", target = "userGender")
    User toUser(SignUpRequest signUpRequest);

    // Custom mapping method for String to UserGender
    default UserGender mapStringToUserGender(String userGender) {
        return userGender != null ? UserGender.valueOf(userGender.toUpperCase()) : null;
    }


    // User -> FindUserResponse
    @Mappings({
            @Mapping(target = "birthDay", dateFormat = "yyyy-MM-dd")
    })
    FindUserResponse of(User user);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "genderVisibility", ignore = true)
    @Mapping(target = "mate", ignore = true)
    @Mapping(target = "dog", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "chatting", ignore = true)
    @Mapping(target = "address1", ignore = true)
    @Mapping(target = "address2", ignore = true)
    @Mapping(target = "mapX", ignore = true)
    @Mapping(target = "mapY", ignore = true)
    User toEntity(String email, String name, String profileImage, RoleType role, String nickname, UserGender userGender, LocalDate birthDay);


}

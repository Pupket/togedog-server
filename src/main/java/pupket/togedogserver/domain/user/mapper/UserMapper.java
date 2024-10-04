package pupket.togedogserver.domain.user.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.dto.request.Preferred;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.*;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // RegistMateRequest -> Mate 매핑
    @Mapping(target = "mateUuid", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "matchCount", ignore = true)
    @Mapping(target = "chatRoom", ignore = true)
    @Mapping(target = "preferredBreeds", ignore = true)
    @Mapping(target = "preferredTimes", ignore = true)
    @Mapping(target = "preferredWeeks", ignore = true)
    @Mapping(target = "mateTags", ignore = true)
    @Mapping(target = "deleted" , ignore = true)
    @Mapping(target = "match", ignore = true)
    @Mapping(target = "preferredRegion", ignore = true)
    Mate toMate(RegistMateRequest request);

    // RegistMateRequest -> Mate 매핑
    @Mapping(target = "mateUuid", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "matchCount", ignore = true)
    @Mapping(target = "chatRoom", ignore = true)
    @Mapping(target = "preferredBreeds", ignore = true)
    @Mapping(target = "preferredTimes", ignore = true)
    @Mapping(target = "preferredWeeks", ignore = true)
    @Mapping(target = "mateTags", ignore = true)
    @Mapping(target = "match", ignore = true)
    Mate toMate(UpdateMateRequest request, User findUser, @MappingTarget Mate findMate);


    // 커스텀 매핑 메서드: PreferredDetailsRequest -> Set<MatePreferredBreed>, Set<MatePreferredTime>, Set<MatePreferredWeek>, Set<MateTag>
    default Mate mapPreferredDetails(Preferred details, Mate mate) {
        return mate.toBuilder()
                .mateUuid(mate.getMateUuid())
                .user(mate.getUser())
                .matchCount(mate.getMatchCount())
                .chatRoom(mate.getChatRoom())
                .preferredBreeds(details.getDogTypes().stream()
                        .map(breed -> MatePreferredBreed.builder().mate(mate).preferredDogType(breed).build())
                        .collect(Collectors.toSet()))
                .preferredTimes(details.getTimes().stream()
                        .map(time -> MatePreferredTime.builder().mate(mate).preferredTime(time).build())
                        .collect(Collectors.toSet()))
                .preferredWeeks(details.getWeeks().stream()
                        .map(week -> MatePreferredWeek.builder().mate(mate).preferredWeek(week).build())
                        .collect(Collectors.toSet()))
                .mateTags(details.getHashTag().stream()
                        .map(style -> MateTag.builder().mate(mate).tagName(style).build())
                        .collect(Collectors.toSet()))
                .preferredRegion(details.getRegion())
                .build();
    }

    @Mapping(target = "userGender", ignore = true)
    @Mapping(target = "platform",ignore = true)
    FindUserResponse of(User user);

    @AfterMapping
    default void afterMapping(@MappingTarget FindUserResponse response, User user) {
        if (user.getUserGender() != null) {
            response.setUserGender(EnumMapper.enumToKorean(user.getUserGender()));
        }

        switch (user.getRole()) {
            case MEMBER_GOOGLE -> response.setPlatform("GOOGLE");
            case MEMBER_KAKAO -> response.setPlatform("KAKAO");
            case MEMBER_NAVER -> response.setPlatform("NAVER");
            default -> response.setPlatform(null);
        }

    }

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "mate", ignore = true)
    @Mapping(target = "dog", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "address1", ignore = true)
    @Mapping(target = "address2", ignore = true)
    @Mapping(target = "mapX", ignore = true)
    @Mapping(target = "mapY", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "fcmToken", ignore = true)
    User toEntity(String email, String name, String profileImage, RoleType role, String nickname, UserGender userGender, int birthday, int birthyear);
}

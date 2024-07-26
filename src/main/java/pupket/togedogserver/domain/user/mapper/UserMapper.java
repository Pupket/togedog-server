package pupket.togedogserver.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.constant.Time;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Week;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.*;

import java.util.Set;
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
    Mate toMate(RegistMateRequest request);


    // 커스텀 매핑 메서드: List<String> -> Set<MatePreferredBreed>
    default Set<MatePreferredBreed> mapPreferredBreeds(Set<Breed> breeds, @MappingTarget Mate mate) {
        return breeds.stream()
                .map(breed -> MatePreferredBreed.builder().mate(mate).preferredBreed(breed).build())
                .collect(Collectors.toSet());
    }

    // 커스텀 매핑 메서드: List<String> -> Set<MatePreferredTime>
    default Set<MatePreferredTime> mapPreferredTimes(Set<Time> times, @MappingTarget Mate mate) {
        return times.stream()
                .map(time -> MatePreferredTime.builder().mate(mate).preferredTime(time).build())
                .collect(Collectors.toSet());
    }

    // 커스텀 매핑 메서드: List<String> -> Set<MatePreferredWeek>
    default Set<MatePreferredWeek> mapPreferredWeeks(Set<Week> weeks, @MappingTarget Mate mate) {
        return weeks.stream()
                .map(week -> MatePreferredWeek.builder().mate(mate).preferredWeek(week).build())
                .collect(Collectors.toSet());
    }

    // 커스텀 매핑 메서드: List<String> -> Set<MateTag>
    default Set<MateTag> mapMateTags(Set<String> tags, @MappingTarget Mate mate) {
        return tags.stream()
                .map(tag -> MateTag.builder().mate(mate).tagName(tag).build())
                .collect(Collectors.toSet());
    }

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
    @Mapping(target = "phoneNumber", ignore = true)
    User toEntity(String email, String name, String profileImage, RoleType role, String nickname, UserGender userGender, int birthday, int birthyear);
}

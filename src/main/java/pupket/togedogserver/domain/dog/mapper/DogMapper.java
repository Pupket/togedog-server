package pupket.togedogserver.domain.dog.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DogMapper {

    @Mapping(target = "dogId", ignore = true) // dogId는 새로 생성되는 엔티티에서만 사용하므로 무시
    @Mapping(target = "user", source = "user") // user를 매핑
    @Mapping(target = "vaccine", ignore = true) // vaccine은 무시
    @Mapping(target = "dogPersonalityTags", ignore = true) // dogPersonalityTags는 별도로 처리
    @Mapping(target = "deleted", ignore = true) // deleted는 기본값으로 설정됨
    @Mapping(target = "dogImage", ignore = true) // dogImage는 기본값으로 설정됨
    @Mapping(target = "name", source = "dogRegistRequest.name")
    Dog toDog(DogRegistRequest dogRegistRequest, User user);

    // DogPersonalityTag 변환 메서드 추가
    default Set<DogPersonalityTag> toDogPersonalityTags(Set<String> tags, Dog dog) {
        return tags.stream()
                .map(tag -> DogPersonalityTag.builder()
                        .tag(tag)
                        .dog(dog)
                        .build())
                .collect(Collectors.toSet());
    }


    @Mapping(target = "dogPersonalityTags", source = "dogPersonalityTags", ignore = true)
    DogResponse toResponse(Dog findDog);



    @AfterMapping
    default void afterMapping(@MappingTarget DogResponse response, Dog dog) {

        response.setDogType(EnumMapper.enumToKorean(dog.getBreed()));

        if (dog.getDogPersonalityTags() != null) {
            response.setDogPersonalityTags(dog.getDogPersonalityTags().stream()
                    .map(DogPersonalityTag::getTag)
                    .collect(Collectors.toSet()));
        }
    }
}
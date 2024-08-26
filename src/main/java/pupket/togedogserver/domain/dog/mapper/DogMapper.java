package pupket.togedogserver.domain.dog.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pupket.togedogserver.domain.dog.constant.Breed;
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

    @Mapping(target = "dogId", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "vaccine", source = "dogRegistRequest.vaccine")
    @Mapping(target = "dogPersonalityTags", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "dogImage", ignore = true)
    @Mapping(target = "breed", expression = "java(mapWeightToBreed(dogRegistRequest.getWeight()))")
    @Mapping(target = "name", source = "dogRegistRequest.name")
    Dog toDog(DogRegistRequest dogRegistRequest, User user);

    // weight에 따른 breed 매핑
    default Breed mapWeightToBreed(int weight) {
        if (weight <= 7) {
            return Breed.SMALL;
        } else if (weight <= 15) {
            return Breed.MID;
        } else if (weight < 40) {
            return Breed.BIG;
        } else {
            return Breed.SUPER;
        }
    }
    // DogPersonalityTag 변환 메서드 추가
    default Set<DogPersonalityTag> toDogPersonalityTags(Set<String> tags, Dog dog) {
        return tags.stream()
                .distinct()
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
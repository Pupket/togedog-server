package pupket.togedogserver.domain.dog.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.dog.dto.request.DogRegistRequest;
import pupket.togedogserver.domain.dog.dto.request.DogUpdateRequest;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.entity.DogPersonalityTag;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DogMapper {

    @Mapping(target = "dogId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "vaccine", ignore = true)
    @Mapping(target = "ownerBoard", ignore = true)
    @Mapping(target = "dogPersonalityTags", ignore = true)
    @Mapping(target = "birthday", source = "birthday", dateFormat = "yyyy-MM-dd")
    Dog toDog(DogRegistRequest dogRegistRequest);

    @AfterMapping
    default void mapTags(@MappingTarget Dog dog, DogRegistRequest dogRegistRequest) {
        List<DogPersonalityTag> tags = dogRegistRequest.getTag().stream()
                .map(tag -> DogPersonalityTag.builder()
                        .tag(tag)
                        .dog(dog)
                        .build())
                .collect(Collectors.toList());

        int weight = dogRegistRequest.getWeight();
        Breed breed = null;
        if (weight >= 40) {
            breed = Breed.SUPER;
        } else if (weight >= 16 && weight < 40) {
            breed = Breed.BIG;
        } else if (weight > 7 && weight <= 15) {
            breed = Breed.MID;
        } else {
            breed = Breed.SMALL;
        }

        dog.toBuilder()
                .dogPersonalityTags(tags)
                .birthday(dogRegistRequest.getBirthday())
                .breed(breed)
                .build();

    }

    void updateDogFromRequest(DogUpdateRequest dogUpdateRequest, @MappingTarget Dog dog);

    DogResponse toResponse(Dog findDog);

}
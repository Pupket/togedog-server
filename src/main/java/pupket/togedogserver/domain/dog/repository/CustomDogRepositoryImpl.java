package pupket.togedogserver.domain.dog.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.dog.dto.response.DogResponse;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomDogRepositoryImpl implements CustomDogRepository {

    private final EntityManager em;


    @Override
    public Page<DogResponse> dogList(Pageable pageable) {
        String query = "SELECT d FROM Dog d" +
                " WHERE d.deleted = false " +
                "order by rand()";

        TypedQuery<Dog> result = em.createQuery(query, Dog.class);

        result.setFirstResult((int) pageable.getOffset());
        result.setMaxResults(pageable.getPageSize());

        List<Dog> results = result.getResultList();

        String countJpql = "SELECT COUNT(b) FROM Mate b WHERE b.deleted = false";
        Long count = em.createQuery(countJpql, Long.class).getSingleResult();


        List<DogResponse> dogResponse = results.stream()
                .map(dog -> {
                    Set<String> list = new HashSet<>();
                    dog.getDogPersonalityTags().forEach(
                            tag -> list.add(tag.getTag())
                    );

                   return  DogResponse.builder()
                            .dogId(dog.getDogId())
                            .name(dog.getName())
                            .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                            .breed(EnumMapper.enumToKorean(dog.getBreed()))
                            .dogGender(dog.getDogGender().equals(true))
                            .neutered(dog.getNeutered())
                            .weight(dog.getWeight())
                            .notes(dog.getNotes())
                            .dogImage(dog.getDogImage())
                            .vaccine(dog.isVaccine())
                            .age(dog.getAge())
                            .dogPersonalityTags(list)
                            .build();
                }).toList();



        return new PageImpl<>(dogResponse, pageable, count);
    }
}

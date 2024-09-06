package pupket.togedogserver.domain.dog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dog_breeds")
public class DogBreed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "breed_name", nullable = false)
    private String breedName;

}

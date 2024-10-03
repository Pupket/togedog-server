package pupket.togedogserver.domain.dog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE dog SET deleted = true WHERE dog_id = ?")
@Where(clause = "deleted = false")
public class Dog {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long dogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_uuid")
    private User user;

    private String breed;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean dogGender;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private Boolean neutered = Boolean.FALSE;

    private int age;

    @Column(nullable = false)
    private Long weight;

    private String notes;

    private String dogImage;

    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean deleted = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    private DogType dogType;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean vaccine = Boolean.FALSE;

    @OneToMany(mappedBy = "dog")
    private Set<DogPersonalityTag> dogPersonalityTags;

    @OneToMany(mappedBy = "dog")
    private List<BoardDog> boardDogs;

}

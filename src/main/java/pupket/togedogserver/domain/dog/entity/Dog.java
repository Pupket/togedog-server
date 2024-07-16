package pupket.togedogserver.domain.dog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import pupket.togedogserver.domain.board.entity.OwnerBoard;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.user.entity.User;
import java.util.Date;
import java.util.List;

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean dogGender;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private Boolean neutered = Boolean.FALSE;

    private Date birthday;

    @Column(nullable = false)
    private Long weight;

    private String notes;

    private String dogImage;

    private boolean Deleted;

    @Enumerated(EnumType.STRING)
    private Breed breed;

    @OneToMany(mappedBy = "dog")
    private List<Vaccine> vaccine;

    @OneToMany(mappedBy = "dog")
    private List<OwnerBoard> ownerBoard;

    @OneToMany(mappedBy = "dog")
    private List<DogPersonalityTag> dogPersonalityTags;
}

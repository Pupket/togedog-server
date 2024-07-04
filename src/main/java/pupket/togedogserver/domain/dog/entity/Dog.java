package pupket.togedogserver.domain.dog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import pupket.togedogserver.domain.board.entity.OwnerBoard;
import pupket.togedogserver.domain.user.entity.User;

import java.sql.Date;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
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
    private Boolean neutered = Boolean.FALSE;

    private Date birthday;

    @Column(nullable = false)
    private Long weight;

    private String notes;

    private String dogImage;

    @OneToMany(mappedBy = "dog")
    private List<Vaccine> vaccine;

    @OneToMany(mappedBy = "dog")
    private List<OwnerBoard> ownerBoard;
}

package pupket.togedogserver.domain.dog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@ToString
public class Vaccine {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long vaccinationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id")
    private Dog dog;

    private String vaccineName;

    private Date vaccinationDate;
}

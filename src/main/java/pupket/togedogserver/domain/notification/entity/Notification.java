package pupket.togedogserver.domain.notification.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.global.baseEntity.BaseEntity;

import java.sql.Timestamp;

@Entity
@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private long alarmId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "image")
    private String image;

    @Column(name="send_Time")
    private Timestamp sendTime;

    @Column(name="room_id")
    private long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uuid")
    private User user;

}

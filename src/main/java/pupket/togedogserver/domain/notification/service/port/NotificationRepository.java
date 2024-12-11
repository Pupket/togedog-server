package pupket.togedogserver.domain.notification.service.port;

import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.notification.entity.Notification;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    void deleteById(Long id);

    List<Notification> findAllByUser(User findUser);
}

package pupket.togedogserver.domain.notification.repository.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.notification.entity.Notification;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;

@Repository
public interface NotificationJPARepository extends JpaRepository<Notification,Long> {

    List<Notification> findAllByUser(User findUser);
}

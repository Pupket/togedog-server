package pupket.togedogserver.domain.notification.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pupket.togedogserver.domain.notification.entity.Notification;
import pupket.togedogserver.domain.notification.repository.jpaRepository.NotificationJPARepository;
import pupket.togedogserver.domain.notification.service.port.NotificationRepository;
import pupket.togedogserver.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJPARepository notificationJPARepository;

    @Override
    public Notification save(Notification notification) {
        return notificationJPARepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationJPARepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
         notificationJPARepository.deleteById(id);
    }

    @Override
    public List<Notification> findAllByUser(User findUser) {
        return notificationJPARepository.findAllByUser(findUser);
    }
}

package pupket.togedogserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.notification.dto.NotificationRequestDto;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final FcmService fcmService;

    @Override
    public void sendNotification(Long sender, NotificationRequestDto notification) throws ExecutionException, InterruptedException {
    }
}

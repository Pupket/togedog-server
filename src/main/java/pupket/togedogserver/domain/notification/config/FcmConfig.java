package pupket.togedogserver.domain.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.FcmException;

import java.io.IOException;

@Component
@Slf4j
public class FcmConfig {

    @Value("${fcm.key.path}")
    private String fcmKeyPath;

    @PostConstruct
    public void fcmInitialize() {
        log.info("fcmkeyPath={}", fcmKeyPath);
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ClassPathResource(fcmKeyPath).getInputStream());
                
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
                
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized successfully");
            } else {
                log.info("Firebase application is already initialized");
            }
        } catch (IOException e) {
            log.error("Firebase initialization failed: {}", e.getMessage());
            throw new FcmException(ExceptionCode.FCM_INITIALIZATION_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error during Firebase initialization: {}", e.getMessage());
            throw new FcmException(ExceptionCode.FCM_INITIALIZATION_ERROR);
        }
    }
}

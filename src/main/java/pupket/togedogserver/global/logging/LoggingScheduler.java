package pupket.togedogserver.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class LoggingScheduler {
    private static final String LOG_DIRECTORY = "logs/logfile.log";
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeLog() {
        try {
            Files.walkFileTree(Paths.get(LOG_DIRECTORY), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws  IOException {
                    LocalDateTime fileTime = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
                    LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

                    if (fileTime.isBefore(oneDayAgo)) {
                        Files.delete(file);
                        System.out.println("Deleted log file: " + file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
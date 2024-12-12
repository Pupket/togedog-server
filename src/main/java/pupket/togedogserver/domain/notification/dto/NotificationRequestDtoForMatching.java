package pupket.togedogserver.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDtoForMatching {
    private Long boardId;
    private Long userId;
    private String title;
    private String message;
    private Timestamp timestamp;
}

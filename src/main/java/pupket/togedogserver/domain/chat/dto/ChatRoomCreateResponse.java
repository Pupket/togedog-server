package pupket.togedogserver.domain.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomCreateResponse {
    Long roomId;
    String nickName;
    String roomTitle;
}

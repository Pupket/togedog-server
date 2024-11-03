package pupket.togedogserver.domain.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class ChatRoomResponseDto {
    Long roomId;
    String title;
    String senderImage;
    String sender;
    String receiver;
    String receiverImage;
    Timestamp lastTime;
     int unreceivedMessageCount;
     String lastMessage;
}

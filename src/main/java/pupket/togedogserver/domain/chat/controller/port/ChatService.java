package pupket.togedogserver.domain.chat.controller.port;

import pupket.togedogserver.domain.chat.dto.ChatRoomCreateResponse;
import pupket.togedogserver.domain.chat.dto.ChatRoomResponseDto;
import pupket.togedogserver.domain.chat.dto.ChattingRequestDto;
import pupket.togedogserver.domain.chat.dto.ChattingResponseDto;

import java.sql.Timestamp;
import java.util.List;

public interface ChatService {
    ChatRoomCreateResponse getOrCreateChatRoom(Long sender, Long receiver, String roomTitle);

    String calculateTimeAgo(Timestamp lastTime);

    List<ChatRoomResponseDto> getChatRoomList(Long uuid);

    void leaveRoom(Long roomId);

    Timestamp getParsedLastTime(String lastTime);

    // 마지막으로 받은 시간 이후의 메시지들을 조회하는 메서드
    List<ChattingResponseDto> getMessagesAfterLastTime(Long roomId, Timestamp lastTime);

    void sendMessageToPublisher(ChattingRequestDto message);
}

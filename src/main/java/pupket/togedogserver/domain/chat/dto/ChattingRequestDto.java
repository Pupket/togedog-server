package pupket.togedogserver.domain.chat.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChattingRequestDto {
    String lastTime;
    Long roomId;
    Long userId;
    String content;
    String image;
}


package pupket.togedogserver.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChattingResponseDto {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    Timestamp lastTime;
    Long userId;
    String content;
    String image;

}

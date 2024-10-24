package pupket.togedogserver.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

    // ISO 8601 형식을 처리하는 커스텀 역직렬화 적용
    @JsonDeserialize(using = CustomISO8601TimestampDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") // ISO 8601 형식
    Timestamp lastTime;
    private Long roomId;
    Long userId;
    String content;
    String image;

}

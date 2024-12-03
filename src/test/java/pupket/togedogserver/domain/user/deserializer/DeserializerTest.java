package pupket.togedogserver.domain.user.deserializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.Time;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Week;
import pupket.togedogserver.global.exception.customException.MemberException;

@ExtendWith(MockitoExtension.class)
class DeserializerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Region Deserializer 성공")
    void regionDeserializer_Success() throws Exception {
        // Given
        String json = "\"서울\"";
        
        // When
        Region result = objectMapper.readValue(json, Region.class);
        
        // Then
        assertThat(result).isEqualTo(Region.SEOUL);
    }

    @Test
    @DisplayName("Region Deserializer 실패 - 잘못된 값")
    void regionDeserializer_Fail() {
        // Given
        String json = "\"invalid\"";
        
        // When & Then
        assertThatThrownBy(() -> 
            objectMapper.readValue(json, Region.class)
        ).isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("Time Deserializer 성공")
    void timeDeserializer_Success() throws Exception {
        // Given
        String json = "\"아침\"";
        
        // When
        Time result = objectMapper.readValue(json, Time.class);
        
        // Then
        assertThat(result).isEqualTo(Time.MORNING);
    }

    @Test
    @DisplayName("Time Deserializer 실패 - 잘못된 값")
    void timeDeserializer_Fail() {
        // Given
        String json = "\"invalid\"";
        
        // When & Then
        assertThatThrownBy(() -> 
            objectMapper.readValue(json, Time.class)
        ).isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("UserGender Deserializer 성공")
    void userGenderDeserializer_Success() throws Exception {
        // Given
        String json = "\"남성\"";
        
        // When
        UserGender result = objectMapper.readValue(json, UserGender.class);
        
        // Then
        assertThat(result).isEqualTo(UserGender.MALE);
    }

    @Test
    @DisplayName("UserGender Deserializer 실패 - 잘못된 값")
    void userGenderDeserializer_Fail() {
        // Given
        String json = "\"invalid\"";
        
        // When & Then
        assertThatThrownBy(() -> 
            objectMapper.readValue(json, UserGender.class)
        ).isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("Week Deserializer 성공")
    void weekDeserializer_Success() throws Exception {
        // Given
        String json = "\"월요일\"";
        
        // When
        Week result = objectMapper.readValue(json, Week.class);
        
        // Then
        assertThat(result).isEqualTo(Week.MON);
    }

    @Test
    @DisplayName("Week Deserializer 실패 - 잘못된 값")
    void weekDeserializer_Fail() {
        // Given
        String json = "\"invalid\"";
        
        // When & Then
        assertThatThrownBy(() -> 
            objectMapper.readValue(json, Week.class)
        ).isInstanceOf(MemberException.class);
    }
}
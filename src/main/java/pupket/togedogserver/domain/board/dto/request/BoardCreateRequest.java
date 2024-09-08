package pupket.togedogserver.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pupket.togedogserver.domain.board.constant.FeeType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "산책 게시판 글 등록 요청")
public class BoardCreateRequest {

    @Schema(description = "게시글 제목", example = "딸랑구 산책 시켜주실분", minLength = 5, maxLength = 20, required = true)
    @Size(min = 5, max = 20, message = "제목은 5자 이상 20자 미만입니다.")
    private String title;

    @Schema(description = "산책 장소 추천 태그", example = "[\"서울대입구\", \"강남역 2번출구\"]", required = true)
    private Set<String> tag;

    @Schema(description = "도로명 주소", example = "서울특별시 관악구 청룡동 ~로", required = true)
    private String pickupLocation1;

//    @Schema(description = "세부 주소", example = "oo아파트 102동 301호", required = true)
//    private String pickupLocation2;

    @Schema(description = "x좌표", example = "24.1298371", required = true)
    private Double mapX;

    @Schema(description = "y좌표", example = "24.1298371", required = true)
    private Double mapY;

    @Schema(description = "강아지 성별 (true: 남, false: 여)", example = "true", required = true)
    private Boolean dogGender;

    @Schema(description = "강아지 고유번호", example = "1", required = true)
    private Long dog_id;

    @Schema(description = "픽업 날짜", example = "2024-12-20", required = true)
    private LocalDate pickUpDay;

    @Schema(description = "시작 시간", example = "12:11:00", required = true, format="time")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "12:11:00", required = true, format="time")
    private LocalTime endTime;

    @Schema(description = "요금 종류(시급,건별)", example = "건별", required = true)
    private FeeType feeType;

    @Schema(description = "요금", example = "20000", required = true)
    private int fee;

    @Schema(description = "연락처", example = "01012345678", required = false)
    @Pattern(regexp = "^[0-9]+$", message = "연락처에는 특수 문자, 공백을 사용할 수 없습니다.")
    private String phoneNumber;
}

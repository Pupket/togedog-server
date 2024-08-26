package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;
import pupket.togedogserver.domain.match.constant.MatchStatus;

/*

(1) 상대가 매칭 요청 수락 시 ‘산책 일정’ 생성
- 산책 일정은 산책 진행 일정(일자) 기준으로 ‘최신순’으로 정렬
(2) 산책 일정 세부 정보
- 매칭된 산책 메이트의 프로필
- 산책메이트 닉네임
- 산책 진행 시간
- 산책 진행 일정(일자)
- 산책 비용
- 산책 메이트와 매칭 성공 시
- 산책 일정 목록 생성
- 진행 현황 : ‘진행 전’, ‘진행 완료’
 */

@Data
@Builder
public class FindMatchedScheduleResponse {

    private Long boardId;
    private String pickUpDay;
    private String startTime;
    private String endTime;
    private String fee;
    private String mateNickname;
    private String matePhotoUrl;
    private Long mateId;
    private MatchStatus matchStatus;


}

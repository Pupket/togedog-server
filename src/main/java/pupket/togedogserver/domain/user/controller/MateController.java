package pupket.togedogserver.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.service.BoardServiceImpl;
import pupket.togedogserver.domain.match.service.MatchServiceImpl;
import pupket.togedogserver.domain.user.controller.port.MateService;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.HashMap;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/mate")
public class MateController {

    private final MateService mateService;
    private final BoardServiceImpl boardServiceImpl;
    private final MatchServiceImpl matchServiceImpl;

    @Operation(summary = "산책 메이트 프로필 등록", description = "산책 메이트 프로필 정보를 등록합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "메이트가 이미 존재하거나 닉네임이 이미 존재함")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Schema(
                    description = "프로필 등록 정보 reqeuest body 양 끝에 \" 을 하나씩 더 붙여야 합니다. 기입이 안되서 설명에 적어놓습니다!",
                    type = "string",
                    example =  "{ " +
                            "\\\"nickname\\\": \\\"sunro1234\\\", " +
                            "\\\"userGender\\\": \\\"여성\\\", " +
                            "\\\"phoneNumber\\\": \\\"12341234\\\", " +
                            "\\\"accommodatableDogsCount\\\": 12, " +
                            "\\\"career\\\": \\\"능수능란\\\", " +
                            "\\\"preferredDetails\\\": { " +
                            "\\\"weeks\\\": [\\\"월요일\\\", \\\"화요일\\\", \\\"수요일\\\"], " +
                            "\\\"times\\\": [\\\"오전\\\", \\\"저녁\\\"], " +
                            "\\\"hashTag\\\": [\\\"귀여운\\\"], " +
                            "\\\"dogTypes\\\": [\\\"중형견\\\", \\\"대형견\\\"], " +
                            "\\\"region\\\": \\\"경상\\\" " +
                            "} " +
                            "}"
            )
            @Valid @RequestPart(value = "request") String requestString,
            @Schema(description = "프로필 이미지 파일", type = "string", format = "binary", nullable = true)
            @RequestPart(value = "profileImage") MultipartFile profileImage) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        RegistMateRequest request = objectMapper.readValue(requestString, RegistMateRequest.class);

        mateService.create(userDetail, request, profileImage);

        return ResponseEntity.status(200).build();
    }

    @Operation(summary = "산책 메이트 프로필 수정", description = "산책 메이트 프로필 정보를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "메이트를 찾을 수 없음 또는 닉네임이 이미 존재함")
    })
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Schema(
                    description = "프로필 등록 정보 reqeuest body 양 끝에 \" 을 하나씩 더 붙여야 합니다. 기입이 안되서 설명에 적어놓습니다!",
                    type = "string",
                    example =  "{ " +
                            "\\\"nickname\\\": \\\"testuser1\\\", " +
                            "\\\"userGender\\\": \\\"남성\\\", " +
                            "\\\"phoneNumber\\\": \\\"01012345678\\\", " +
                            "\\\"accommodatableDogsCount\\\": 5, " +
                            "\\\"career\\\": \\\"강형욱보다 더 잘함\\\", " +
                            "\\\"preferredDetails\\\": { " +
                            "\\\"weeks\\\": [\\\"월요일\\\", \\\"화요일\\\", \\\"금요일\\\"], " +
                            "\\\"times\\\": [\\\"새벽\\\", \\\"저녁\\\"], " +
                            "\\\"hashTag\\\": [\\\"사악함\\\"], " +
                            "\\\"dogTypes\\\": [\\\"소형견\\\", \\\"대형견\\\"], " +
                            "\\\"region\\\": \\\"전라\\\" " +
                            "} " +
                            "}"
            )
            @RequestPart(value = "request") String updateMateRequest,
            @RequestPart(value = "profileImage") MultipartFile profileImage
    ) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        UpdateMateRequest request = objectMapper.readValue(updateMateRequest, UpdateMateRequest.class);

        mateService.update(userDetail, request, profileImage);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "산책 메이트 프로필 조회", description = "산책 메이트 프로필 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "메이트를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<FindMateResponse> find(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        FindMateResponse findMateResponse = mateService.find(userDetail);

        return ResponseEntity.ok().body(findMateResponse);
    }

    @Operation(summary = "산책 메이트 랜덤 반환", description = "산책 메이트 프로필을 랜덤으로 반환합니다. (페이지 시작 0부터, 사이즈 4부터 시작)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 랜덤 반환 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })

    @GetMapping("/random")
    public ResponseEntity<Page<FindMateResponse>> findRandom(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Parameter(description = "페이지 시작 번호(0부터 시작)")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 사이즈(4부터 시작)")
            @RequestParam(name = "size", defaultValue = "4") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FindMateResponse> mateList = mateService.findRandom(pageable,userDetail);

        return ResponseEntity.ok().body(mateList);
    }

    @Operation(summary = "산책 메이트 삭제", description = "산책 메이트 프로필을 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "메이트 태그를 찾을 수 없음")
    })
    @DeleteMapping()
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        mateService.delete(userDetail);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "산책 메이트 닉네임 중복 체크", description = "산책 메이트 닉네임 중복 여부 체크")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 중복체크 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @Parameter(name = "nickname", description = "닉네임", example = "surno123", required = true, schema = @Schema(type = "string"))
    @GetMapping("/{nickname}")
    public ResponseEntity<HashMap<String,Object>> checkNickName(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable("nickname")
            String nickname
    ) {
        boolean flag = mateService.checkNickname(userDetail, nickname);

        HashMap<String,Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("flag" , String.valueOf(flag));

        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "산책 메이트 닉네임 자동 완성", description = "산책 메이트 닉네임 자동 완성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<List<String>> autoCompleteKeyword(
            @PathVariable("keyword") String keyword
    )
    {
        List<String> result = mateService.autocorrect(keyword);

        return ResponseEntity.ok().body(result);
    }

    @Operation(summary = "산책 메이트 내 일정 조회", description = "산책 메이트 내 일정 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/mySchedule")
    public ResponseEntity<Page<BoardFindResponse>> findMySchedule(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Parameter(description = "페이지 시작 번호(0부터 시작)")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 사이즈(5부터 시작)")
            @RequestParam(name = "size", defaultValue = "5") int size,
            @Parameter(description = "첫 요청 여부")
            @RequestParam(name = "first", defaultValue = "false") boolean first
    ) {
        if (first) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);

        Page<BoardFindResponse> boardList = boardServiceImpl.findMySchedule(userDetail, pageable);

        return ResponseEntity.ok().body(boardList);
    }

    @Operation(summary = "산책 완료", description = "산책 완료")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "산책 완료 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/complete")
    public ResponseEntity<Page<FindMateResponse>> walkingComplete(
            @Parameter(name = "boardId", description = "게시판 Id")
            Long boardId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        matchServiceImpl.completeWalking(boardId,userDetail);

        return ResponseEntity.ok().build();
    }


}
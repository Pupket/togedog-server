package pupket.togedogserver.domain.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.dto.request.BoardCreateRequest;
import pupket.togedogserver.domain.board.dto.request.BoardUpdateRequest;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.board.mapper.BoardMapper;
import pupket.togedogserver.domain.board.repository.BoardRepository;
import pupket.togedogserver.domain.board.repository.CustomBoardRepositoryImpl;
import pupket.togedogserver.domain.board.repository.WalkingPlaceTagRepository;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.repository.DogRepository;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.domain.user.repository.mateRepo.CustomMateRepositoryImpl;
import pupket.togedogserver.domain.user.repository.mateRepo.MateRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.BoardException;
import pupket.togedogserver.global.exception.customException.DogException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.exception.customException.WalkingPlaceTagException;
import pupket.togedogserver.global.mapper.EnumMapper;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardMapper boardMapper;
    private final WalkingPlaceTagRepository walkingPlaceTagRepository;
    private final DogRepository dogRepository;
    private final CustomBoardRepositoryImpl customBoardRepositoryImpl;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomMateRepositoryImpl customMateRepositoryImpl;
    private final MateRepository mateRepository;

    @Override
    public void create(CustomUserDetail userDetail, BoardCreateRequest boardCreateRequest) {
        User findUser = getUserById(userDetail.getUuid());

        Dog findDog = dogRepository.findById(boardCreateRequest.getDog_id()).orElseThrow(() ->
                new DogException(ExceptionCode.NOT_FOUND_DOG));

        Board mapperBoard = boardMapper.toBoard(boardCreateRequest);
        boardRepository.save(mapperBoard);

        Board savedDogBoard = mapperBoard.toBuilder().dog(findDog).build();

        Board newMapperBoard = saveTags(boardCreateRequest, savedDogBoard, findUser);

        boardRepository.save(newMapperBoard);
    }

    public BoardFindResponse find(CustomUserDetail userDetail, Long boardId) {
        // 유저 찾기
        User findUser = getUserById(userDetail.getUuid());

        // 보드와 도그 찾기
        Board findBoard = boardRepository.findByUserAndBoardId(findUser, boardId).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );
        Dog findDog = dogRepository.findByBoards(findBoard).orElseThrow(
                () -> new DogException(ExceptionCode.NOT_FOUND_DOG)
        );

        // 필요한 변환 작업 수행
        String fee = findBoard.getFee().toString();
        String startTime = findBoard.getStartTime().toString();
        String endTime = findBoard.getEndTime().toString();
        String feeType = EnumMapper.enumToKorean(findBoard.getFeeType());
        String dogType = EnumMapper.enumToKorean(findDog.getDogType());
        String breed = EnumMapper.enumToKorean(findDog.getBreed());
        List<String> walkingPlaceTags = findBoard.getWalkingPlaceTag().stream()
                .map(WalkingPlaceTag::getPlaceName)
                .collect(Collectors.toList());

        // BoardFindResponse 객체 생성
        return BoardFindResponse.builder()
                .boardId(findBoard.getBoardId())
                .userId(findBoard.getUser().getUuid())
                .title(findBoard.getTitle())
                .pickUpDay(findBoard.getPickUpDay())
                .fee(fee)
                .age(findDog.getAge())
                .startTime(startTime)
                .endTime(endTime)
                .pickupLocation1(findBoard.getPickupLocation1())
                .walkingPlaceTag(walkingPlaceTags)
                .feeType(feeType)
                .name(findDog.getName())
                .dogType(dogType)
                .breed(breed)
                .dogGender(findDog.getDogGender()?"수컷":"암컷")
                .dogProfileImage(findDog.getDogImage())
                .build();
    }


    public void update(CustomUserDetail userDetail, BoardUpdateRequest request) {
        User findUser = getUserById(userDetail.getUuid());
        Board findBoard = boardRepository.findByUserAndBoardId(findUser, request.getId()).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );
        Dog findDog = dogRepository.findById(request.getDog_id()).orElseThrow(
                () -> new DogException(ExceptionCode.NOT_FOUND_DOG)
        );

        // Remove existing tags associated with the board
        List<WalkingPlaceTag> existingTags = walkingPlaceTagRepository.findAllByBoard(findBoard).orElseThrow(
                () -> new WalkingPlaceTagException(ExceptionCode.NOT_FOUND_WALKING_PLACE_TAG)
        );
        walkingPlaceTagRepository.deleteAll(existingTags);

        Board newBoard = findBoard.toBuilder()
                .title(request.getTitle())
                .mapX(request.getMapX())
                .mapY(request.getMapY())
                .pickupLocation1(request.getPickupLocation1())
                .dog(findDog)
                .pickUpDay(request.getPickUpDay())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .feeType(request.getFeeType())
                .fee(request.getFee())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Board savedBoard = boardRepository.save(newBoard);

        saveTags(request, savedBoard);

    }

    private void saveTags(BoardUpdateRequest request, Board savedBoard) {
        Set<WalkingPlaceTag> newTags = request.getTag().stream()
                .map(tag -> WalkingPlaceTag.builder()
                        .placeName(tag)
                        .board(savedBoard)
                        .build())
                .collect(Collectors.toSet());

        walkingPlaceTagRepository.saveAll(newTags);
    }

    private Board saveTags(BoardCreateRequest boardCreateRequest, Board mapperBoard, User findUser) {
        Set<WalkingPlaceTag> tags = boardCreateRequest.getTag().stream()
                .map(tag -> WalkingPlaceTag.builder()
                        .placeName(tag)
                        .board(mapperBoard)
                        .build())
                .collect(Collectors.toSet());

        Board newMapperBoard = mapperBoard.toBuilder()
                .user(findUser)
                .walkingPlaceTag(tags)
                .build();

        walkingPlaceTagRepository.saveAll(tags);
        return newMapperBoard;
    }

    private User getUserById(Long uuid) {
        refreshTokenRepository.getRefreshTokenByMemberId(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_REFRESH_TOKEN)
        );
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }

    public void delete(CustomUserDetail userDetail, Long id) {
        User findUser = getUserById(userDetail.getUuid());

        Board findBoard = boardRepository.findByUserAndBoardId(findUser, id).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );

        List<WalkingPlaceTag> findWalkingPlaceTag = walkingPlaceTagRepository.findAllByBoard(findBoard).orElseThrow(
                () -> new WalkingPlaceTagException(ExceptionCode.NOT_FOUND_WALKING_PLACE_TAG)
        );

        walkingPlaceTagRepository.deleteAll(findWalkingPlaceTag);
        boardRepository.delete(findBoard);
    }

    public Page<BoardFindResponse> findRandom(Pageable pageable) {
        return customBoardRepositoryImpl.BoardList(pageable);
    }

    public Page<BoardFindResponse> findMySchedule(CustomUserDetail userDetail, Pageable pageable) {
        User findUser = getUserById(userDetail.getUuid());
        Mate findMate = mateRepository.findByUser(findUser).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MATE)
        );

        return customMateRepositoryImpl.findMyScheduleList(findMate.getMateUuid(), pageable);
    }
}

package pupket.togedogserver.domain.board.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.controller.port.BoardService;
import pupket.togedogserver.domain.board.dto.request.BoardCreateRequest;
import pupket.togedogserver.domain.board.dto.request.BoardUpdateRequest;
import pupket.togedogserver.domain.board.dto.response.BoardDogResponse;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.board.mapper.BoardMapper;
import pupket.togedogserver.domain.board.service.port.BoardDogRepository;
import pupket.togedogserver.domain.board.service.port.BoardRepository;
import pupket.togedogserver.domain.board.service.port.CustomBoardRepository;
import pupket.togedogserver.domain.board.service.port.WalkingPlaceTagRepository;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.domain.dog.repository.jpaRepository.DogJPARepository;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.service.port.CustomMateRepository;
import pupket.togedogserver.domain.user.service.port.MateRepository;
import pupket.togedogserver.domain.user.service.port.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.BoardException;
import pupket.togedogserver.global.exception.customException.DogException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.exception.customException.WalkingPlaceTagException;
import pupket.togedogserver.global.mapper.EnumMapper;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final WalkingPlaceTagRepository walkingPlaceTagRepository;
    private final BoardMapper boardMapper;
    private final DogJPARepository dogJPARepository;
    private final CustomBoardRepository customBoardRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomMateRepository customMateRepository;
    private final MateRepository mateRepository;
    private final BoardDogRepository boardDogRepository;

    @Override
    public void create(CustomUserDetail userDetail, BoardCreateRequest boardCreateRequest) {
        log.info("Creating board for user: {}", userDetail.getUuid());
        User findUser = getUserById(userDetail.getUuid());

        List<Dog> dogList = validateEachDog(boardCreateRequest.getDogIds(), findUser);

        Board mapperBoard = boardMapper.toBoard(boardCreateRequest);
        boardRepository.save(mapperBoard);

        List<BoardDog> boardDogList = mapDogsToBoard(dogList, mapperBoard);

        Board savedBoard = saveTags(boardCreateRequest, mapperBoard, findUser);

        boardRepository.save(savedBoard.toBuilder().boardDog(boardDogList).build());
        log.info("Board created successfully for user: {}", userDetail.getUuid());
    }

    private List<Dog> validateEachDog(List<Long> dogIds, User findUser) {
        log.debug("Validating dogs for user: {}", findUser.getUuid());
        return dogIds.stream().map(dogId -> {
            Dog findDog = dogJPARepository.findById(dogId).orElseThrow(() ->
                    new DogException(ExceptionCode.NOT_FOUND_DOG));
            if (!Objects.equals(findDog.getUser().getUuid(), findUser.getUuid())) {
                throw new DogException(ExceptionCode.NOT_YOUR_DOG);
            }
            return findDog;
        }).toList();
    }

    @Override
    public BoardFindResponse find(CustomUserDetail userDetail, Long boardId) {
        log.info("Finding board with ID: {} for user: {}", boardId, userDetail.getUuid());
        getUserById(userDetail.getUuid());

        // 보드 찾기
        Board findBoard = findBoard(boardId);

        if (!isPresent(findBoard)) {
            throw new DogException(ExceptionCode.NOT_FOUND_DOG);
        }

        // 해당 보드에 속한 도그 목록 찾기
        List<Dog> findDogs = getDogListByBoard(findBoard);

        // 여러 마리의 개 정보를 DogResponse로 변환
        List<BoardDogResponse> boardDogRespons = getBoardDogResponses(findDogs);

        log.info("Board found successfully with ID: {}", boardId);
        return BoardFindResponse.to(findBoard, boardDogRespons);
    }

    private static List<Dog> getDogListByBoard(Board findBoard) {
        return findBoard.getBoardDog().stream()
                .map(BoardDog::getDog)
                .toList();
    }

    private static boolean isPresent(Board findBoard) {
        return findBoard.getBoardDog().stream()
                .map(BoardDog::getDog)
                .findAny()
                .isPresent();
    }

    private Board findBoard(Long boardId) {
        return boardRepository.findByBoardId(boardId).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );
    }

    private static List<BoardDogResponse> getBoardDogResponses(List<Dog> findDogs) {
        return findDogs.stream()
                .map(dog -> BoardDogResponse.builder()
                        .name(dog.getName())
                        .age(dog.getAge())
                        .breed(dog.getBreed())
                        .dogType(EnumMapper.enumToKorean(dog.getDogType()))
                        .dogGender(dog.getDogGender() ? "수컷" : "암컷")
                        .dogProfileImage(dog.getDogImage())
                        .build())
                .toList();
    }

    @Transactional
    @Override
    public void update(CustomUserDetail userDetail, BoardUpdateRequest boardUpdateRequest) {
        log.info("Updating board with ID: {} for user: {}", boardUpdateRequest.getId(), userDetail.getUuid());
        User findUser = getUserById(userDetail.getUuid());

        //게시판 찾기
        Board findBoard = getFindBoardByUpdateRequest(boardUpdateRequest, findUser);

        //각 강아지 유효성 검사
        List<Dog> dogList = validateEachDog(boardUpdateRequest.getDogIds(), findUser);

        //이전 저장 태그 모두 삭제
        boardDogRepository.deleteAllByBoard(findBoard);

        //게시판에 각 강아지엔티티 연결
        List<BoardDog> boardDogList = mapDogsToBoard(dogList, findBoard);

        //산책지역태그 찾기
        List<WalkingPlaceTag> existingTags = getWalkingPlaceTags(findBoard);

        //기존의 태그 모두 삭제
        walkingPlaceTagRepository.deleteAll(existingTags);

        Board newBoard = updateBoard(boardUpdateRequest, findBoard, boardDogList);

        saveTags(boardUpdateRequest, newBoard);

        boardRepository.save(newBoard);
        log.info("Board updated successfully with ID: {}", boardUpdateRequest.getId());
    }

    private List<WalkingPlaceTag> getWalkingPlaceTags(Board findBoard) {
        return walkingPlaceTagRepository.findAllByBoard(findBoard).orElseThrow(
                () -> new WalkingPlaceTagException(ExceptionCode.NOT_FOUND_WALKING_PLACE_TAG)
        );
    }

    private Board getFindBoardByUpdateRequest(BoardUpdateRequest boardUpdateRequest, User findUser) {
        return boardRepository.findByUserAndBoardId(findUser, boardUpdateRequest.getId()).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );
    }

    private static Board updateBoard(BoardUpdateRequest boardUpdateRequest, Board findBoard, List<BoardDog> boardDogList) {
       return findBoard.toBuilder()
                .title(boardUpdateRequest.getTitle())
                .mapX(boardUpdateRequest.getMapX())
                .mapY(boardUpdateRequest.getMapY())
                .pickupLocation1(boardUpdateRequest.getPickupLocation1())
                .boardDog(boardDogList)
                .pickUpDay(boardUpdateRequest.getPickUpDay())
                .startTime(boardUpdateRequest.getStartTime())
                .endTime(boardUpdateRequest.getEndTime())
                .feeType(boardUpdateRequest.getFeeType())
                .fee(boardUpdateRequest.getFee())
                .phoneNumber(boardUpdateRequest.getPhoneNumber())
                .build();
    }

    private List<BoardDog> mapDogsToBoard(List<Dog> dogList, Board board) {
        List<BoardDog> boardDogList = dogList.stream()
                .map(dog -> BoardDog.builder().board(board).dog(dog).build())
                .toList();
        boardDogRepository.saveAll(boardDogList);
        return boardDogList;
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

    @Override
    public void delete(CustomUserDetail userDetail, Long id) {
        log.info("Deleting board with ID: {} for user: {}", id, userDetail.getUuid());
        User findUser = getUserById(userDetail.getUuid());

        Board findBoard = boardRepository.findByUserAndBoardId(findUser, id).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );

        List<WalkingPlaceTag> findWalkingPlaceTag = getWalkingPlaceTags(findBoard);

        walkingPlaceTagRepository.deleteAll(findWalkingPlaceTag);
        boardRepository.delete(findBoard);
    }

    @Override
    public Page<BoardFindResponse> findRandom(Pageable pageable) {
        return customBoardRepository.findRandomBoardList(pageable);
    }

    @Override
    public Page<BoardFindResponse> findMySchedule(CustomUserDetail userDetail, Pageable pageable) {
        User findUser = getUserById(userDetail.getUuid());

        Mate findMate = findMate(findUser);

        return customMateRepository.findMyScheduleList(findMate.getMateUuid(), pageable);
    }

    private Mate findMate(User findUser) {
        return mateRepository.findByUser(findUser).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MATE)
        );
    }
}

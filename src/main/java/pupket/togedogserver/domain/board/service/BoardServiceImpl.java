package pupket.togedogserver.domain.board.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.board.dto.request.BoardCreateRequest;
import pupket.togedogserver.domain.board.dto.request.BoardUpdateRequest;
import pupket.togedogserver.domain.board.dto.response.BoardDogResponse;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.BoardDog;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.board.mapper.BoardMapper;
import pupket.togedogserver.domain.board.repository.BoardDogRepository;
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
    private final BoardMapper boardMapper;
    private final WalkingPlaceTagRepository walkingPlaceTagRepository;
    private final DogRepository dogRepository;
    private final CustomBoardRepositoryImpl customBoardRepositoryImpl;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomMateRepositoryImpl customMateRepositoryImpl;
    private final MateRepository mateRepository;
    private final BoardDogRepository boardDogRepository;

    @Override
    public void create(CustomUserDetail userDetail, BoardCreateRequest boardCreateRequest) {
        User findUser = getUserById(userDetail.getUuid()); //유저 엔티티 반환

        List<Dog> dogList = validateEachDog(boardCreateRequest.getDogIds(), findUser); //각 강아지 유효성 검사

        Board mapperBoard = boardMapper.toBoard(boardCreateRequest); // Board 엔티티 생성

        boardRepository.save(mapperBoard);

        List<BoardDog> boardDogList = mapDogsToBoard(dogList, mapperBoard); //BoardDog 엔티티 생성

        Board savedBoard = saveTags(boardCreateRequest, mapperBoard, findUser); // 게시판에 태그 저장

        boardRepository.save(savedBoard.toBuilder().boardDog(boardDogList).build()); // 각 엔티티 연결 및 저장
    }

    private List<Dog> validateEachDog(List<Long> dogIds, User findUser) {
        return dogIds.stream().map(dogId -> {
            Dog findDog = dogRepository.findById(dogId).orElseThrow(() ->
                    new DogException(ExceptionCode.NOT_FOUND_DOG));
            if (!Objects.equals(findDog.getUser().getUuid(), findUser.getUuid())) {
                throw new DogException(ExceptionCode.NOT_YOUR_DOG);
            }
            return findDog;
        }).collect(Collectors.toList());
    }

    public BoardFindResponse find(CustomUserDetail userDetail, Long boardId) {
        // 유저 찾기
        User findUser = getUserById(userDetail.getUuid());

        // 보드 찾기
        Board findBoard = boardRepository.findByBoardId(boardId).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );

        // 보드에 속한 도그가 존재하는지 확인하고, 없으면 예외 처리
        boolean hasDogs = findBoard.getBoardDog().stream()
                .map(BoardDog::getDog)
                .findAny()
                .isPresent();

        if (!hasDogs) {
            throw new DogException(ExceptionCode.NOT_FOUND_DOG);
        }

        // 해당 보드에 속한 도그 목록 찾기
        List<Dog> findDogs = findBoard.getBoardDog().stream()
                .map(BoardDog::getDog)
                .collect(Collectors.toList());

        // Board의 기본 정보 처리
        String fee = findBoard.getFee().toString();
        String startTime = findBoard.getStartTime().toString();
        String endTime = findBoard.getEndTime().toString();
        String feeType = EnumMapper.enumToKorean(findBoard.getFeeType());
        List<String> walkingPlaceTags = findBoard.getWalkingPlaceTag().stream()
                .map(WalkingPlaceTag::getPlaceName)
                .collect(Collectors.toList());

        // 여러 마리의 개 정보를 DogResponse로 변환
        List<BoardDogResponse> boardDogRespons = getBoardDogResponses(findDogs);

        // BoardFindResponse 객체 생성
        return BoardFindResponse.builder()
                .boardId(findBoard.getBoardId())
                .userId(findBoard.getUser().getUuid())
                .title(findBoard.getTitle())
                .pickUpDay(findBoard.getPickUpDay())
                .fee(fee)
                .startTime(startTime)
                .endTime(endTime)
                .pickupLocation1(findBoard.getPickupLocation1())
                .walkingPlaceTag(walkingPlaceTags)
                .feeType(feeType)
                .dogs(boardDogRespons) // 여러 마리의 개 정보 추가
                .build();
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
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(CustomUserDetail userDetail, BoardUpdateRequest boardUpdateRequest) {
        //유저 찾기
        User findUser = getUserById(userDetail.getUuid());

        //게시판 찾기
        Board findBoard = boardRepository.findByUserAndBoardId(findUser, boardUpdateRequest.getId()).orElseThrow(
                () -> new BoardException(ExceptionCode.NOT_FOUND_BOARD)
        );

        //각 강아지 유효성 검사
        List<Dog> dogList = validateEachDog(boardUpdateRequest.getDogIds(), findUser);

        //이전 저장 태그 모두 삭제
        boardDogRepository.deleteAllByBoard(findBoard);

        //게시판에 각 강아지엔티티 연결
        List<BoardDog> boardDogList = mapDogsToBoard(dogList, findBoard);

        //산책지역태그 찾기
        List<WalkingPlaceTag> existingTags = walkingPlaceTagRepository.findAllByBoard(findBoard).orElseThrow(
                () -> new WalkingPlaceTagException(ExceptionCode.NOT_FOUND_WALKING_PLACE_TAG)
        );

        //기존의 태그 모두 삭제
        walkingPlaceTagRepository.deleteAll(existingTags);

        Board newBoard = updateBoard(boardUpdateRequest, findBoard, boardDogList);

        saveTags(boardUpdateRequest, newBoard);

        boardRepository.save(newBoard);

    }

    private static Board updateBoard(BoardUpdateRequest boardUpdateRequest, Board findBoard, List<BoardDog> boardDogList) {
        Board newBoard = findBoard.toBuilder()
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
        return newBoard;
    }

    private List<BoardDog> mapDogsToBoard(List<Dog> dogList, Board board) {
        List<BoardDog> boardDogList = dogList.stream()
                .map(dog -> BoardDog.builder().board(board).dog(dog).build())
                .collect(Collectors.toList());
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

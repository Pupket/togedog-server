package pupket.togedogserver.domain.user.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.dto.response.PreferredDetailsResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MateTag;
import pupket.togedogserver.domain.user.mapper.UserMapper;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.domain.user.repository.mateRepo.*;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MateException;
import pupket.togedogserver.global.exception.customException.MateTagException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.mapper.EnumMapper;
import pupket.togedogserver.global.redis.RedisSortedSetService;
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class MateServiceImpl implements MateService {

    private final UserRepository userRepository;
    private final MatePreferredBreedRepository matePreferredBreedRepository;
    private final MatePreferredWeekRepository matePreferredWeekRepository;
    private final MatePreferredTimeRepository matePreferredTimeRepository;
    private final MateRepository mateRepository;
    private final MateTagRepository mateTagRepository;
    private final CustomMateRepositoryImpl customMateRepositoryImpl;
    private final UserMapper userMapper;
    private final S3FileUtilImpl s3FileUtilImpl;
    private final RefreshTokenRepository refreshTokenRepository;

    private final String suffix = "*";
    private final RedisSortedSetService redisSortedSetService;


    @PostConstruct
    public void init() {    //이 Service Bean이 생성된 이후에 검색어 자동 완성 기능을 위한 데이터들을 Redis에 저장 (Redis는 인메모리 DB라 휘발성을 띄기 때문)
        List<String> allUserNickname = userRepository.findAllNickname();
        log.info("size={}", allUserNickname.size());
        saveAllSubstring(allUserNickname); //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직

    }

    private void saveAllSubstring(List<String> userNickName) { //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직
        // long start1 = System.currentTimeMillis(); //뒤에서 성능 비교를 위해 시간을 재는 용도
        for (String name : userNickName) {
            redisSortedSetService.addToSortedSetFromMate(name + suffix);   //완벽한 형태의 단어일 경우에는 *을 붙여 구분

            for (int i = name.length(); i > 0; --i) { //음절 단위로 잘라서 모든 Substring 구하기
                redisSortedSetService.addToSortedSetFromMate(name.substring(0, i)); //곧바로 redis에 저장
            }
        }
        // long end1 = System.currentTimeMillis(); //뒤에서 성능 비교를 위해 시간을 재는 용도
        // long elapsed1 = end1 - start1;  //뒤에서 성능 비교를 위해 시간을 재는 용도
    }

    @Override
    public void create(CustomUserDetail userDetail, RegistMateRequest request, MultipartFile profileImage) {
        User findUser = getUserById(userDetail.getUuid());

        validateUserAndNickname(request, findUser); //Mate중복 여부 및 닉네임 중복 검사

        String uploadedProfileImage = uploadProfileImage(profileImage); //이미지 유효성 검사 및 업로드

        findUser = updateUser(request, findUser, uploadedProfileImage); //유저 이미지 및 기타 정보 업데이트

        Mate createdMate = userMapper.toMate(request); //Mate 생성

        createdMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), createdMate); //customMapper로 preferred엔티티 맵핑

        Mate savedMate = TwoWayMappingUserAndMate(createdMate, findUser); //양방향 맵핑(유저, 메이트)

        saveMatePreferences(savedMate, request); //각 태그 영속성 저장

        mateRepository.save(savedMate);
    }

    private Mate TwoWayMappingUserAndMate(Mate createdMate, User findUser) {
        Mate updatedMate = connectWithUser(createdMate, findUser);

        mateRepository.save(updatedMate);

        User connectedUser = connectWithMate(findUser, updatedMate);

        userRepository.save(connectedUser);

        return updatedMate;
    }

    private static Mate connectWithUser(Mate createdMate, User findUser) {
        //mate와 user 양방향 맵핑
        return createdMate.toBuilder() //mate와 user 양방향 맵핑
                .user(findUser)
                .build();
    }

    private User connectWithMate(User findUser, Mate updatedMate) {
        return findUser.toBuilder()
                .mate(updatedMate)
                .build();
    }

    private User updateUser(RegistMateRequest request, User findUser, String uploadedProfileImage) {

        if (findUser.getRole().equals(RoleType.MEMBER_GOOGLE) && !request.getBirthday().isEmpty()) {
            String[] splitBirthArr = request.getBirthday().split("\\.");
            int birthyear = Integer.parseInt(splitBirthArr[0]);
            int birthday = Integer.parseInt(splitBirthArr[1] + splitBirthArr[2]);

            findUser = findUser.toBuilder()
                    .nickname(request.getNickname())
                    .profileImage(uploadedProfileImage)
                    .userGender(request.getUserGender())
                    .phoneNumber(request.getPhoneNumber())
                    .birthyear(birthyear)
                    .birthday(birthday)
                    .build();
        } else {
            findUser = findUser.toBuilder()
                    .nickname(request.getNickname())
                    .profileImage(uploadedProfileImage)
                    .userGender(request.getUserGender())
                    .phoneNumber(request.getPhoneNumber())
                    .build();
        }


        findUser = userRepository.save(findUser);
        return findUser;
    }

    private String uploadProfileImage(MultipartFile profileImage) {
        String uploadedProfileImage = null;
        if (profileImage != null) {
            uploadedProfileImage = s3FileUtilImpl.upload(profileImage);

        }
        return uploadedProfileImage;
    }

    private void validateUserAndNickname(RegistMateRequest request, User findUser) {
        mateRepository.findByUser(findUser).ifPresent(mate -> {
            throw new MateException(ExceptionCode.MATE_ALREADY_EXIST);
        });

        userRepository.findByNickname(request.getNickname()).ifPresent(user -> {
            throw new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS);
        });
    }

    @Override
    public FindMateResponse find(CustomUserDetail userDetail) {
        User finduser = getUserById(userDetail.getUuid());
        Mate findMate = mateRepository.findByUser(finduser).orElse(null);
        if (findMate == null) {
            return null;
        }

        PreferredDetailsResponse preferredDetails = getPreferredDetailsResponse(findMate); //Mate 태그 객체 생성

        String birthday = digitCustomize(findMate); //생일 두자리수로 맞추기

        return FindMateResponse.builder()
                .uuid(findMate.getUser().getUuid())
                .mateId(findMate.getMateUuid())
                .nickname(findMate.getUser().getNickname())
                .profileImage(findMate.getUser().getProfileImage())
                .gender(EnumMapper.enumToKorean(findMate.getUser().getUserGender()))  // Convert gender to Korean
                .age(LocalDateTime.now().getYear() - findMate.getUser().getBirthyear())
                .accommodatableDogsCount(findMate.getAccommodatableDogsCount())
                .career(findMate.getCareer())
                .preferred(preferredDetails)
                .birth(findMate.getUser().getBirthyear() + "." + birthday.substring(0, 2) + "." + birthday.substring(2, 4))
                .build();
    }

    private static PreferredDetailsResponse getPreferredDetailsResponse(Mate findMate) {
        return PreferredDetailsResponse.builder()
                .week(findMate.getPreferredWeeks().stream()
                        .map(week -> EnumMapper.enumToKorean(week.getPreferredWeek()))
                        .collect(Collectors.toSet()))
                .time(findMate.getPreferredTimes().stream()
                        .map(time -> EnumMapper.enumToKorean(time.getPreferredTime()))
                        .collect(Collectors.toSet()))
                .hashTag(findMate.getMateTags().stream()
                        .map(MateTag::getTagName)
                        .collect(Collectors.toSet()))
                .breed(findMate.getPreferredBreeds().stream()
                        .map(breed -> EnumMapper.enumToKorean(breed.getPreferredDogType()))
                        .collect(Collectors.toSet()))
                .region(EnumMapper.enumToKorean(findMate.getPreferredRegion()))
                .build();
    }

    private static String digitCustomize(Mate findMate) {
        // birthday를 4자리로 맞추기 (3자리면 앞에 0 추가)
        String birthday = String.valueOf(findMate.getUser().getBirthday());
        if (birthday.length() == 3) {
            birthday = "0" + birthday; // 앞에 0을 붙여 4자리로 만듦
        }
        return birthday;
    }

    @Override
    public Page<FindMateResponse> findRandom(Pageable pageable) {

        return customMateRepositoryImpl.MateList(pageable);

    }

    @Override
    public void update(CustomUserDetail userDetail, UpdateMateRequest request, MultipartFile profileImage) {
        User findUser = getUserById(userDetail.getUuid());

        log.info("nicknmae쿼리");
        validateNickname(request, findUser); //nickname 중복 검사

        log.info("redis 업데이트 쿼리");
        deleteOldNicknameFromRedis(findUser); //Redis에 유저 닉네임 최신화

        log.info("profileImage 처리");
        String uploadedProfileImage = updateProfileImage(profileImage, findUser); //profileImage 최신화

        log.info("유저 정보 업데이트 쿼리");
        findUser = updateUserByRequest(request, findUser, uploadedProfileImage);// 유저 정보 업데이트 (닉네임 변경 포함)

        saveNewNicknameInRedis(findUser); // Redis에 새로운 닉네임 정보 저장

        log.info("mate 호출 쿼리");
        Mate findMate = getMate(findUser);

        log.info("tag들 삭제 쿼리");
        deleteTags(findMate);

        findMate = userMapper.toMate(request, findUser, findMate);

        Mate savedMate = findMate.toBuilder()
                .career(request.getCareer())
                .accommodatableDogsCount(request.getAccommodatableDogsCount())
                .build();

        savedMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), savedMate);

        mateRepository.save(savedMate);

        saveMatePreferences(savedMate, request);
    }

    private Mate getMate(User findUser) {
        return mateRepository.findByUser(findUser)
                .orElseThrow(() -> new MateException(ExceptionCode.NOT_FOUND_MATE));
    }

    private void saveNewNicknameInRedis(User findUser) {
        List<String> newNicknames = new ArrayList<>();
        newNicknames.add(findUser.getNickname());
        saveAllSubstring(newNicknames);
    }

    private User updateUserByRequest(UpdateMateRequest request, User findUser, String uploadedProfileImage) {
        if (findUser.getRole().equals(RoleType.MEMBER_GOOGLE) && !request.getBirthday().isEmpty()) {
            String[] splitBirthArr = request.getBirthday().split("\\.");
            int birthyear = Integer.parseInt(splitBirthArr[0]);
            int birthday = Integer.parseInt(splitBirthArr[1] + splitBirthArr[2]);

            findUser = findUser.toBuilder()
                    .nickname(request.getNickname())
                    .profileImage(uploadedProfileImage)
                    .userGender(request.getUserGender())
                    .phoneNumber(request.getPhoneNumber())
                    .birthyear(birthyear)
                    .birthday(birthday)
                    .build();
        } else {
            findUser = findUser.toBuilder()
                    .nickname(request.getNickname())
                    .profileImage(uploadedProfileImage)
                    .userGender(request.getUserGender())
                    .phoneNumber(request.getPhoneNumber())
                    .build();
        }


        findUser = userRepository.save(findUser);
        return findUser;
    }

    private String updateProfileImage(MultipartFile profileImage, User findUser) {
        // 프로필 이미지 삭제 및 업로드 로직
        if (findUser.getProfileImage() != null) {
            s3FileUtilImpl.deleteImageFromS3(findUser.getProfileImage());
        }

        return uploadProfileImage(profileImage);
    }

    private void validateNickname(UpdateMateRequest request, User findUser) {
        // 기존 닉네임 중복 체크 로직
        if (!findUser.getNickname().equals(request.getNickname())) {
            if (userRepository.findByNickname(request.getNickname()).isPresent()) {
                throw new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS);
            }
        }
    }

    private void deleteOldNicknameFromRedis(User findUser) {
        // Redis에서 기존 닉네임 정보 삭제
        List<String> oldNicknames = new ArrayList<>();
        oldNicknames.add(findUser.getNickname());
        deleteNicknameFromRedis(oldNicknames);
    }

    private void deleteNicknameFromRedis(List<String> oldNicknames) {
        for (String name : oldNicknames) {
            // 기존 닉네임을 Redis에서 삭제
            redisSortedSetService.removeFromSortedSetFromMate(name + suffix); // 기존 닉네임 전체 삭제

            for (int i = name.length(); i > 0; --i) { // 음절 단위로 잘라서 모든 Substring을 Redis에서 삭제
                redisSortedSetService.removeFromSortedSetFromMate(name.substring(0, i));
            }
        }
    }

    private void deleteTags(Mate findMate) {
        mateTagRepository.deleteAllByMate(findMate);
        matePreferredBreedRepository.deleteAllByMate(findMate);
        matePreferredTimeRepository.deleteAllByMate(findMate);
        matePreferredWeekRepository.deleteAllByMate(findMate);
    }

    @Override
    public void delete(CustomUserDetail userDetail) {

        User findUser = getUserById(userDetail.getUuid());

        Mate findMate = getMate(findUser);

        List<MateTag> findMateTag = mateTagRepository.findAllByMate(findMate).orElseThrow(
                () -> new MateTagException(ExceptionCode.NOT_FOUND_MATE_TAG)
        );

        if (findUser.getProfileImage() != null) {
            s3FileUtilImpl.deleteImageFromS3(findUser.getProfileImage());
        }

        mateRepository.delete(findMate);
        mateTagRepository.deleteAll(findMateTag);
    }

    private User getUserById(Long uuid) {
        refreshTokenRepository.getRefreshTokenByMemberId(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_REFRESH_TOKEN)
        );
        return userRepository.findByUuid(uuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }

    private void saveMatePreferences(Mate savedMate, RegistMateRequest request) {
        Mate updatedMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), savedMate);

        matePreferredBreedRepository.saveAll(updatedMate.getPreferredBreeds());
        matePreferredTimeRepository.saveAll(updatedMate.getPreferredTimes());
        matePreferredWeekRepository.saveAll(updatedMate.getPreferredWeeks());
        mateTagRepository.saveAll(updatedMate.getMateTags());

    }

    private void saveMatePreferences(Mate savedMate, UpdateMateRequest request) {
        Mate updatedMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), savedMate);
        log.info("모든 태그 저장 쿼리");
        matePreferredBreedRepository.saveAll(updatedMate.getPreferredBreeds());
        matePreferredTimeRepository.saveAll(updatedMate.getPreferredTimes());
        matePreferredWeekRepository.saveAll(updatedMate.getPreferredWeeks());
        mateTagRepository.saveAll(updatedMate.getMateTags());

    }

    public boolean checkNickname(CustomUserDetail userDetail, String nickname) {
        User findUser = getUserById(userDetail.getUuid());

        //내 닉네임은 그대로 사용할 수 있게 true로 반환
        return findUser.getNickname().equals(nickname) || userRepository.findByNickname(nickname).isEmpty();
    }

    public List<String> autoCompleteKeyword(String keyword) {
        return autocorrect(keyword);
    }

    public List<String> autocorrect(String keyword) { //검색어 자동 완성 기능 관련 로직
        Long index = redisSortedSetService.findFromSortedSetFromMate(keyword);  //사용자가 입력한 검색어를 바탕으로 Redis에서 조회한 결과 매칭되는 index
        if (index == null) {
            log.info("index가 비어있음");
            return new ArrayList<>();   //만약 사용자 검색어 바탕으로 자동 완성 검색어를 만들 수 없으면 Empty Array 리턴
        }

        Set<String> allValuesAfterIndexFromSortedSet = redisSortedSetService.findAllValuesInMateAfterIndexFromSortedSet(index);   //사용자 검색어 이후로 정렬된 Redis 데이터들 가져오기

        //자동 완성을 통해 만들어진 최대 maxSize 개의 키워드들
        //검색어 자동 완성 기능 최대 개수
        int maxSize = 100;

        return allValuesAfterIndexFromSortedSet.stream()
                .filter(value -> value.endsWith(suffix) && value.startsWith(keyword))
                .map(this::removeEnd)
                .limit(maxSize)
                .toList();
    }

    private String removeEnd(String str) {
        if (str != null && str.endsWith("*")) {
            return str.substring(0, str.length() - "*".length());
        }
        return str;
    }
}

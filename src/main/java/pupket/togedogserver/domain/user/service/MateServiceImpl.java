package pupket.togedogserver.domain.user.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.user.constant.RoleType;
import pupket.togedogserver.domain.user.controller.port.MateService;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.mapper.UserMapper;
import pupket.togedogserver.domain.user.repository.MateRepositoryImpl;
import pupket.togedogserver.domain.user.repository.MateTagRepositoryImpl;
import pupket.togedogserver.domain.user.service.port.*;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MateException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.redis.RedisSortedSetService;
import pupket.togedogserver.global.s3.util.S3FileUtil;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class MateServiceImpl implements MateService {

    private final UserRepository userRepository;
    private final MatePreferredBreedRepository matePreferredBreedRepository;
    private final MatePreferredWeekRepository matePreferredWeekRepository;
    private final MatePreferredTimeRepository matePreferredTimeRepository;
    private final MateRepositoryImpl mateRepository;
    private final MateTagRepositoryImpl mateTagRepository;
    private final CustomMateRepository customMateRepository;
    private final UserMapper userMapper;
    private final S3FileUtil s3FileUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityManager entityManager;

    private final String suffix = "*";
    private final RedisSortedSetService redisSortedSetService;

    private static Mate connectWithUser(Mate createdMate, User findUser) {
        //mate와 user 양방향 맵핑
        return createdMate.toBuilder() //mate와 user 양방향 맵핑
                .user(findUser)
                .build();
    }

    @PostConstruct
    public void init() {    //이 Service Bean이 생성된 이후에 검색어 자동 완성 기능을 위한 데이터들을 Redis에 저장 (Redis는 인메모리 DB라 휘발성을 띄기 때문)
        List<String> nicknames = userRepository.findAllNicknames();
        log.info("size={}", nicknames.size());
        saveAllSubstring(nicknames); //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직

    }

    private void saveAllSubstring(List<String> userNickName) { //MySQL DB에 저장된 모든 가게명을 음절 단위로 잘라 모든 Substring을 Redis에 저장해주는 로직
        // long start1 = System.currentTimeMillis(); //뒤에서 성능 비교를 위해 시간을 재는 용도
        for (String name : userNickName) {
            redisSortedSetService.addToSortedSetFromMate(name + suffix);   //완벽한 형태의 단어일 경우에는 *을 붙여 구분

            for (int i = name.length(); i > 0; --i) { //음절 단위로 잘라서 모든 Substring 구하기
                redisSortedSetService.addToSortedSetFromMate(name.substring(0, i)); //곧바로 redis에 저장
            }
        }
    }

    @Override
    public List<String> autoCompleteKeyword(String keyword) {
        log.info("자동 완성 키워드 요청: {}", keyword);
        return autocorrect(keyword);
    }

    @Override
    public List<String> autocorrect(String keyword) { //검색어 자동 완성 기능 관련 로직
        Long index = redisSortedSetService.findFromSortedSetFromMate(keyword);  //사용자가 입력한 검색어를 바탕으로 Redis에서 조회한 결과 매칭되는 index
        if (index == null) {
            log.info("index가 비어있음");
            return new ArrayList<>();   //만약 사용자 검색어 바탕으로 자동 완성 검색어를 만들 수 없으면 Empty Array 리턴
        }

        Set<String> allValuesAfterIndexFromSortedSet = redisSortedSetService.findAllValuesInMateAfterIndexFromSortedSet(index);   //사용자 검색어 이후로 정렬된 Redis 데이터들 가져오기

        //자동 완성을 통해 만들어진 최대 maxSize 개의 키워드들
        //검색어 자동 완성 기능 최대 개수
        int maxSize = 2000;

        return allValuesAfterIndexFromSortedSet.stream()
                .filter(value -> value.endsWith(suffix) && value.startsWith(keyword))
                .map(this::removeEnd)
                .limit(maxSize)
                .toList();
    }

    @Override
    public void create(CustomUserDetail userDetail, RegistMateRequest request, MultipartFile profileImage) {
        log.info("메이트 생성 시작: 사용자 ID = {}", userDetail.getUuid());
        User findUser = getUserById(userDetail.getUuid());

        validateUserAndNickname(request, findUser); //Mate중복 여부 및 닉네임 중복 검사

        String uploadedProfileImage = uploadProfileImage(profileImage); //이미지 유효성 검사 및 업로드

        findUser = updateUser(request, findUser, uploadedProfileImage); //유저 이미지 및 기타 정보 업데이트

        Mate createdMate = userMapper.toMate(request); //Mate 생성

        createdMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), createdMate); //customMapper로 preferred엔티티 맵핑

        Mate savedMate = TwoWayMappingUserAndMate(createdMate, findUser); //양방향 맵핑(유저, 메이트)

        saveMatePreferences(savedMate, request); //각 태그 영속성 저장

        mateRepository.save(savedMate);
        log.info("메이트 생성 완료: 사용자 ID = {}", userDetail.getUuid());
    }

    private Mate TwoWayMappingUserAndMate(Mate createdMate, User findUser) {
        Mate updatedMate = connectWithUser(createdMate, findUser);

        updatedMate= mateRepository.save(updatedMate);

        User connectedUser = connectWithMate(findUser, updatedMate);

        connectedUser=userRepository.save(connectedUser);

        return updatedMate;
    }

    private User connectWithMate(User findUser, Mate updatedMate) {
        return findUser.toBuilder()
                .mate(updatedMate)
                .build();
    }

    private User updateUser(RegistMateRequest request, User findUser, String uploadedProfileImage) {
        log.info("유저 정보 업데이트 시작: 사용자 ID = {}", findUser.getUuid());

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
        log.info("유저 정보 업데이트 완료: 사용자 ID = {}", findUser.getUuid());
        return findUser;
    }

    private String uploadProfileImage(MultipartFile profileImage) {
        String uploadedProfileImage = null;
        if (profileImage != null) {
            log.info("프로필 이미지 업로드 시작");
            uploadedProfileImage = s3FileUtil.upload(profileImage);
            log.info("프로필 이미지 업로드 완료");
        }
        return uploadedProfileImage;
    }

    private void validateUserAndNickname(RegistMateRequest request, User findUser) {
        log.info("유저 및 닉네임 유효성 검사 시작: 사용자 ID = {}", findUser.getUuid());
        mateRepository.findByUser(findUser).ifPresent(mate -> {
            throw new MateException(ExceptionCode.MATE_ALREADY_EXIST);
        });

        userRepository.findByNickname(request.getNickname()).ifPresent(user -> {
            throw new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS);
        });
        log.info("유저 및 닉네임 유효성 검사 완료: 사용자 ID = {}", findUser.getUuid());
    }

    @Override
    public FindMateResponse find(CustomUserDetail userDetail) {
        log.info("메이트 조회 시작: 사용자 ID = {}", userDetail.getUuid());
        User finduser = getUserById(userDetail.getUuid());
        Mate findMate = mateRepository.findByUser(finduser).orElse(null);
        if (findMate == null) {
            log.info("메이트가 존재하지 않음: 사용자 ID = {}", userDetail.getUuid());
            return null;
        }

        log.info("메이트 조회 완료: 사용자 ID = {}", userDetail.getUuid());
        return FindMateResponse.to(findMate);
    }

    @Override
    public Page<FindMateResponse> findRandom(Pageable pageable) {
        log.info("랜덤 메이트 조회 시작");
        Page<FindMateResponse> result = customMateRepository.MateList(pageable);
        log.info("랜덤 메이트 조회 완료");
        return result;
    }

    @Override
    public void update(CustomUserDetail userDetail, UpdateMateRequest request, MultipartFile profileImage) {
        log.info("메이트 업데이트 시작: 사용자 ID = {}", userDetail.getUuid());
        User findUser = getUserById(userDetail.getUuid());

        validateNickname(request, findUser); //nickname 중복 검사

        deleteOldNicknameFromRedis(findUser); //Redis에 유저 닉네임 최신화

        String uploadedProfileImage = updateProfileImage(profileImage, findUser); //profileImage 최신화

        findUser = updateUserByRequest(request, findUser, uploadedProfileImage);// 유저 정보 업데이트 (닉네임 변경 포함)

        saveNewNicknameInRedis(findUser); // Redis에 새로운 닉네임 정보 저장

        Mate findMate = getMate(findUser);

        deleteTags(findMate);

        findMate = userMapper.toMate(request, findUser, findMate);

        Mate savedMate = findMate.toBuilder()
                .career(request.getCareer())
                .accommodatableDogsCount(request.getAccommodatableDogsCount())
                .build();

        savedMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), savedMate);

        savedMate = mateRepository.save(savedMate);

        saveMatePreferences(savedMate, request);
        log.info("메이트 업데이트 완료: 사용자 ID = {}", userDetail.getUuid());
    }

    private Mate getMate(User findUser) {
        return mateRepository.findByUser(findUser)
                .orElseThrow(() -> new MateException(ExceptionCode.NOT_FOUND_MATE));
    }

    private void saveNewNicknameInRedis(User findUser) {
        log.info("새로운 닉네임 Redis에 저장 시작: 사용자 ID = {}", findUser.getUuid());
        List<String> newNicknames = new ArrayList<>();
        newNicknames.add(findUser.getNickname());
        saveAllSubstring(newNicknames);
        log.info("새로운 닉네임 Redis에 저장 완료: 사용자 ID = {}", findUser.getUuid());
    }

    private User updateUserByRequest(UpdateMateRequest request, User findUser, String uploadedProfileImage) {
        log.info("유저 정보 업데이트 시작: 사용자 ID = {}", findUser.getUuid());
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
        log.info("유저 정보 업데이트 완료: 사용자 ID = {}", findUser.getUuid());
        return findUser;
    }

    private String updateProfileImage(MultipartFile profileImage, User findUser) {
        log.info("프로필 이미지 업데이트 시작: 사용자 ID = {}", findUser.getUuid());
        // 프로필 이미지 삭제 및 업로드 로직
        if (findUser.getProfileImage() != null) {
            s3FileUtil.deleteImageFromS3(findUser.getProfileImage());
        }

        String newProfileImage = uploadProfileImage(profileImage);
        log.info("프로필 이미지 업데이트 완료: 사용자 ID = {}", findUser.getUuid());
        return newProfileImage;
    }

    private void validateNickname(UpdateMateRequest request, User findUser) {
        log.info("닉네임 유효성 검사 시작: 사용자 ID = {}", findUser.getUuid());
        // 기존 닉네임 중복 체크 로직
        if (!findUser.getNickname().equals(request.getNickname())) {
            if (userRepository.findByNickname(request.getNickname()).isPresent()) {
                throw new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS);
            }
        }
        log.info("닉네임 유효성 검사 완료: 사용자 ID = {}", findUser.getUuid());
    }

    private void deleteOldNicknameFromRedis(User findUser) {
        log.info("기존 닉네임 Redis에서 삭제 시작: 사용자 ID = {}", findUser.getUuid());
        // Redis에서 기존 닉네임 정보 삭제
        List<String> oldNicknames = new ArrayList<>();
        oldNicknames.add(findUser.getNickname());
        deleteNicknameFromRedis(oldNicknames);
        log.info("기존 닉네임 Redis에서 삭제 완료: 사용자 ID = {}", findUser.getUuid());
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
        log.info("메이트 태그 삭제 시작: 메이트 ID = {}", findMate.getMateUuid());
        mateTagRepository.deleteAllByMate(findMate);
        matePreferredBreedRepository.deleteAllByMate(findMate);
        matePreferredTimeRepository.deleteAllByMate(findMate);
        matePreferredWeekRepository.deleteAllByMate(findMate);
        log.info("메이트 태그 삭제 완료: 메이트 ID = {}", findMate.getMateUuid());
    }

    @Override
    @Transactional
    public void delete(CustomUserDetail userDetail) {
        log.info("메이트 삭제 시작: 사용자 ID = {}", userDetail.getUuid());
        User findUser = getUserById(userDetail.getUuid());

        if (findUser.getProfileImage() != null) {
            s3FileUtil.deleteImageFromS3(findUser.getProfileImage());
        }
        Mate findMate = getMate(findUser);
        userRepository.save(findUser.toBuilder().mate(null).build());
        log.info("User와 Mate 관계 해제 완료: 사용자 ID = {}", userDetail.getUuid());

        log.info("findMateTag삭제 수행");
        mateTagRepository.deleteAll(findMate.getMateTags());
        matePreferredBreedRepository.deleteAllByMate(findMate);
        matePreferredTimeRepository.deleteAllByMate(findMate);
        matePreferredWeekRepository.deleteAllByMate(findMate);


        log.info("findMate삭제 수행");
        findMate = findMate.toBuilder().user(null).build();
        findMate = mateRepository.save(findMate);
        entityManager.flush();
        mateRepository.delete(findMate);
        log.info("메이트 삭제 완료: 사용자 ID = {}", userDetail.getUuid());
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
        log.info("메이트 선호도 저장 시작: 메이트 ID = {}", savedMate.getMateUuid());
        Mate updatedMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), savedMate);
        updatedMate = mateRepository.save(updatedMate);

        matePreferredBreedRepository.saveAll(updatedMate.getPreferredBreeds());
        matePreferredTimeRepository.saveAll(updatedMate.getPreferredTimes());
        matePreferredWeekRepository.saveAll(updatedMate.getPreferredWeeks());
        mateTagRepository.saveAll(updatedMate.getMateTags());
        log.info("메이트 선호도 저장 완료: 메이트 ID = {}", savedMate.getMateUuid());
    }

    private void saveMatePreferences(Mate savedMate, UpdateMateRequest request) {
        log.info("모든 태그 저장 쿼리");
        matePreferredBreedRepository.saveAll(savedMate.getPreferredBreeds());
        matePreferredTimeRepository.saveAll(savedMate.getPreferredTimes());
        matePreferredWeekRepository.saveAll(savedMate.getPreferredWeeks());
        mateTagRepository.saveAll(savedMate.getMateTags());
        log.info("메이트 선호도 저장 완료: 메이트 ID = {}", savedMate.getMateUuid());
    }

    @Override
    public boolean checkNickname(CustomUserDetail userDetail, String nickname) {
        log.info("닉네임 중복 체크 시작: 사용자 ID = {}, 닉네임 = {}", userDetail.getUuid(), nickname);
        User findUser = getUserById(userDetail.getUuid());

        //내 닉네임은 그대로 사용할 수 있게 true로 반환
        boolean result = findUser.getNickname().equals(nickname) || userRepository.findByNickname(nickname).isEmpty();
        log.info("닉네임 중복 체크 완료: 사용자 ID = {}, 닉네임 = {}, 결과 = {}", userDetail.getUuid(), nickname, result);
        return result;
    }

    private String removeEnd(String str) {
        if (str != null && str.endsWith("*")) {
            return str.substring(0, str.length() - "*".length());
        }
        return str;
    }
}

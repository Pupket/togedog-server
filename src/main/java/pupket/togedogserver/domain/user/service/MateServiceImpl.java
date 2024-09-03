package pupket.togedogserver.domain.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
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
import pupket.togedogserver.global.s3.util.S3FileUtilImpl;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.time.LocalDateTime;
import java.util.List;
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
    private final UserServiceImpl userServiceImpl;
    private final CustomMateRepositoryImpl customMateRepositoryImpl;
    private final UserMapper userMapper;
    private final S3FileUtilImpl s3FileUtilImpl;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void create(CustomUserDetail userDetail, RegistMateRequest request, MultipartFile profileImage) {
        User findUser = getUserById(userDetail.getUuid());

        mateRepository.findByUser(findUser).ifPresent(mate -> {
            throw new MateException(ExceptionCode.MATE_ALREADY_EXIST);
        });

        userRepository.findByNickname(request.getNickname()).ifPresent(user -> {
            throw new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS);
        });


        String uploadedProfileImage = null;
        if(profileImage != null) {
            uploadedProfileImage=s3FileUtilImpl.upload(profileImage);

        }


        findUser = findUser.toBuilder()
                .nickname(request.getNickname())
                .profileImage(uploadedProfileImage)
                .userGender(request.getUserGender())
                .genderVisibility(request.getGenderVisibility())
                .phoneNumber(request.getPhoneNumber())
                .build();

        findUser = userRepository.save(findUser);

        Mate createdMate = userMapper.toMate(request);
        createdMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), createdMate);


        Mate updatedMate = createdMate.toBuilder()
                .user(findUser)
                .build();

        Mate savedMate = mateRepository.save(updatedMate);

        saveMatePreferences(savedMate, request);
    }

    @Override
    public FindMateResponse find(CustomUserDetail userDetail) {
        User finduser = getUserById(userDetail.getUuid());
        Mate findMate = mateRepository.findByUser(finduser).orElseThrow(
                () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
        );

        PreferredDetailsResponse preferredDetails = PreferredDetailsResponse.builder()
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
                .build();

        return FindMateResponse.builder()
                .nickname(findMate.getUser().getNickname())
                .profileImage(findMate.getUser().getProfileImage())
                .gender(EnumMapper.enumToKorean(findMate.getUser().getUserGender()))  // Convert gender to Korean
                .region(EnumMapper.enumToKorean(findMate.getRegion()))  // Convert region to Korean
                .age(LocalDateTime.now().getYear() - findMate.getUser().getBirthyear())
                .accommodatableDogsCount(findMate.getAccommodatableDogsCount())
                .career(findMate.getCareer())
                .preferred(preferredDetails)
                .preferredRegion(EnumMapper.enumToKorean(findMate.getRegion()))
                .birth(findMate.getUser().getBirthyear()+"."+String.valueOf(findMate.getUser().getBirthday()).substring(0,2)+"."+String.valueOf(findMate.getUser().getBirthday()).substring(2,4))
                .build();
    }


    @Override
    public Page<FindMateResponse> findRandom(Pageable pageable) {

        return customMateRepositoryImpl.MateList(pageable);

    }

    @Override
    public void update(CustomUserDetail userDetail, UpdateMateRequest request, MultipartFile profileImage) {
        User findUser = getUserById(userDetail.getUuid());

        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            if (!findUser.getNickname().equals(request.getNickname())) {
                throw new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS);
            }
        }
        if (findUser.getProfileImage() != null) {
            s3FileUtilImpl.deleteImageFromS3(findUser.getProfileImage());
        }

        String uploadedProfileImage = null;
        if (profileImage != null) {
            uploadedProfileImage = s3FileUtilImpl.upload(profileImage);
        }
            findUser = findUser.toBuilder()
                    .nickname(request.getNickname())
                    .phoneNumber(request.getPhoneNumber())
                    .genderVisibility((request.getGenderVisibility()))
                    .profileImage(uploadedProfileImage)
                    .build();

        userRepository.save(findUser);

        Mate findMate = mateRepository.findByUser(findUser)
                .orElseThrow(() -> new MateException(ExceptionCode.NOT_FOUND_MATE));

        deleteTags(findMate);

        findMate = userMapper.toMate(request, findUser,findMate);

        Mate savedMate = findMate.toBuilder()
                .career(request.getCareer())
                .region(request.getRegion())
                .accommodatableDogsCount(request.getAccommodatableDogsCount())
                .build();

        savedMate = mateRepository.save(savedMate);

        savedMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), savedMate);

        savedMate = mateRepository.save(savedMate);

        saveMatePreferences(savedMate, request);

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

        Mate findMate = mateRepository.findByUser(findUser).orElseThrow(
                () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
        );

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

        mateRepository.save(updatedMate);
    }

    private void saveMatePreferences(Mate savedMate, UpdateMateRequest request) {
        Mate updatedMate = userMapper.mapPreferredDetails(request.getPreferredDetails(), savedMate);

        matePreferredBreedRepository.saveAll(updatedMate.getPreferredBreeds());
        matePreferredTimeRepository.saveAll(updatedMate.getPreferredTimes());
        matePreferredWeekRepository.saveAll(updatedMate.getPreferredWeeks());
        mateTagRepository.saveAll(updatedMate.getMateTags());

        mateRepository.save(updatedMate);
    }

    public boolean checkNickname(CustomUserDetail userDetail, String nickname) {
        User findUser = getUserById(userDetail.getUuid());

        return findUser.getNickname().equals(nickname) || userRepository.findByNickname(nickname).isEmpty();
    }
}

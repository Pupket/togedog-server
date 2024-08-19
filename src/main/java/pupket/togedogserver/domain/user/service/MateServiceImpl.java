package pupket.togedogserver.domain.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.dto.response.PreferredDetailsResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.*;
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
    private final UserServiceImpl userServiceImpl;
    private final CustomMateRepositoryImpl customMateRepositoryImpl;
    private final UserMapper userMapper;
    private final S3FileUtilImpl s3FileUtilImpl;

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

        Mate updatedMate = createdMate.toBuilder()
                .user(findUser)
                .preferredBreeds(userMapper.mapPreferredBreeds(request.getPreferredBreed(), createdMate))
                .preferredTimes(userMapper.mapPreferredTimes(request.getPreferredTime(), createdMate))
                .preferredWeeks(userMapper.mapPreferredWeeks(request.getPreferredWeek(), createdMate))
                .mateTags(userMapper.mapMateTags(request.getPreferredStyle(), createdMate))
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
                .style(findMate.getMateTags().stream()
                        .map(MateTag::getTagName)
                        .collect(Collectors.toSet()))
                .breed(findMate.getPreferredBreeds().stream()
                        .map(breed -> EnumMapper.enumToKorean(breed.getPreferredBreed()))
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

        findMate = findMate.toBuilder()
                .user(findUser)
                .accommodatableDogsCount(request.getAccommodatableDogsCount())
                .career(request.getCareer())
                .region(request.getRegion())
                .preferredBreeds(userMapper.mapPreferredBreeds(request.getPreferredBreed(), findMate))
                .preferredTimes(userMapper.mapPreferredTimes(request.getPreferredTime(), findMate))
                .preferredWeeks(userMapper.mapPreferredWeeks(request.getPreferredWeek(), findMate))
                .mateTags(userMapper.mapMateTags(request.getPreferredStyle(), findMate))
                .build();

        Mate savedMate = mateRepository.save(findMate);

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

    private User getUserById(Long memberUuid) {
        return userRepository.findByUuid(memberUuid).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
    }


    private void saveMatePreferences(Mate savedMate, RegistMateRequest request) {
        Set<MatePreferredBreed> preferredBreeds = userMapper.mapPreferredBreeds(request.getPreferredBreed(), savedMate);
        Set<MatePreferredTime> preferredTimes = userMapper.mapPreferredTimes(request.getPreferredTime(), savedMate);
        Set<MatePreferredWeek> preferredWeeks = userMapper.mapPreferredWeeks(request.getPreferredWeek(), savedMate);
        Set<MateTag> mateTags = userMapper.mapMateTags(request.getPreferredStyle(), savedMate);

        matePreferredBreedRepository.saveAll(preferredBreeds);
        matePreferredTimeRepository.saveAll(preferredTimes);
        matePreferredWeekRepository.saveAll(preferredWeeks);
        mateTagRepository.saveAll(mateTags);

        // 업데이트된 Mate 저장
        savedMate.toBuilder()
                .preferredBreeds(preferredBreeds)
                .preferredWeeks(preferredWeeks)
                .preferredTimes(preferredTimes)
                .build();

        mateRepository.save(savedMate);
    }


    private void saveMatePreferences(Mate savedMate, UpdateMateRequest request) {
        Set<MatePreferredBreed> preferredBreeds = userMapper.mapPreferredBreeds(request.getPreferredBreed(), savedMate);
        Set<MatePreferredTime> preferredTimes = userMapper.mapPreferredTimes(request.getPreferredTime(), savedMate);
        Set<MatePreferredWeek> preferredWeeks = userMapper.mapPreferredWeeks(request.getPreferredWeek(), savedMate);
        Set<MateTag> mateTags = userMapper.mapMateTags(request.getPreferredStyle(), savedMate);

        matePreferredBreedRepository.saveAll(preferredBreeds);
        matePreferredTimeRepository.saveAll(preferredTimes);
        matePreferredWeekRepository.saveAll(preferredWeeks);
        mateTagRepository.saveAll(mateTags);

        // 업데이트된 Mate 저장
        savedMate.toBuilder()
                .preferredBreeds(preferredBreeds)
                .preferredWeeks(preferredWeeks)
                .preferredTimes(preferredTimes)
                .build();

        mateRepository.save(savedMate);
    }

    public boolean checkNickname(CustomUserDetail userDetail, String nickname) {
        User findUser = getUserById(userDetail.getUuid());

        return userRepository.findByNickname(nickname).isEmpty() || !findUser.getNickname().equals(nickname);
    }
}

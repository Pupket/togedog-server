package pupket.togedogserver.domain.user.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.Time;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Week;
import pupket.togedogserver.domain.user.dto.request.RegistMateRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateMateRequest;
import pupket.togedogserver.domain.user.dto.response.FindMateResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.entity.mate.*;
import pupket.togedogserver.domain.user.repository.*;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MateException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class MateServiceImpl implements MateService {

    private static final Logger log = LoggerFactory.getLogger(MateServiceImpl.class);
    private final UserRepository userRepository;
    private final MatePreferredBreedRepository matePreferredBreedRepository;
    private final MatePreferredWeekRepository matePreferredWeekRepository;
    private final MatePreferredTimeRepository matePreferredTimeRepository;
    private final MateRepository mateRepository;
    private final MateTagRepository mateTagRepository;
    private final UserServiceImpl userServiceImpl;

    @Override
    public void create(CustomUserDetail userDetail, RegistMateRequest request) {
        User findUser = getUserById(userDetail.getUuid());

        mateRepository.findByUser(findUser).orElseThrow(() ->
                new MateException(ExceptionCode.MATE_ALREADY_EXIST)
        );

        userRepository.findByNickname(request.getNickname()).orElseThrow(() ->
                new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS)
        );

        User savedUser = saveUpdatedUser(findUser, request);

        Mate savedMate = updatedMate(savedUser, request);

        saveMatePreferences(savedMate, request);


    }


    @Override
    public FindMateResponse find(CustomUserDetail userDetail) {
        User finduser = getUserById(userDetail.getUuid());
        Mate findMate = mateRepository.findByUser(finduser).orElseThrow(
                () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
        );

        FindMateResponse findMateResponse = FindMateResponse.builder()
                .nickname(findMate.getUser().getNickname())
                .age(findMate.getUser().getAge())
                .profileImage(findMate.getUser().getProfileImage())
                .gender(String.valueOf(findMate.getUser().getUserGender()))
                .region(String.valueOf(findMate.getRegion()))
                .preferredStyle(findMate.getMateTag().stream().map(tag -> tag.getTagName()).collect(Collectors.toSet()))
                .preferredTime(findMate.getPreferredTimes().stream().map(time -> time.getPreferredTime().toString()).collect(Collectors.toSet()))
                .preferredWeek(findMate.getPreferredWeeks().stream().map(week -> week.getPreferredWeek().toString()).collect(Collectors.toSet()))
                .preferredBreed(findMate.getPreferredBreed().stream().map(breed -> breed.getPreferredBreed().toString()).collect(Collectors.toSet()))
                .build();


        return findMateResponse;
    }

    @Override
    public void update(CustomUserDetail userDetail, UpdateMateRequest request) {
        User findUser = getUserById(userDetail.getUuid());

        userRepository.findByNickname(request.getNickname()).orElseThrow(
                () -> new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS)
        );

        User savedUser = saveUpdatedUser(findUser, request);

        mateRepository.findByUser(savedUser).orElseThrow(
                () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
        );

        updatedMate(findUser, request);
        Mate savedMate = updatedMate(findUser, request);
        saveMatePreferences(savedMate, request);


    }

    @Override
    public void delete(CustomUserDetail userDetail) {

        User findUser = getUserById(userDetail.getUuid());

        Mate findMate = mateRepository.findByUser(findUser).orElseThrow(
                () -> new MateException(ExceptionCode.NOT_FOUND_MATE)
        );

        mateRepository.delete(findMate);

    }

    private User getUserById(Long memberUuid) {
        return userRepository.findByUuid(memberUuid).
                orElseThrow(
                        () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
                );
    }


    private User saveUpdatedUser(User findUser, UpdateMateRequest request) {
        User updatedUser = findUser.toBuilder()
                .nickname(request.getNickname())
                .age(request.getAge())
                .userGender(UserGender.valueOf(request.getUserGender()))
                .phoneNumber(request.getPhoneNumber())
                .build();

        return userRepository.save(updatedUser);
    }

    private User saveUpdatedUser(User findUser, RegistMateRequest request) {
        User updatedUser = findUser.toBuilder()
                .nickname(request.getNickname())
                .age(request.getAge())
                .userGender(UserGender.valueOf(request.getUserGender()))
                .phoneNumber(request.getPhoneNumber())
                .build();

        return userRepository.save(updatedUser);
    }

    private Mate updatedMate(User savedUser, RegistMateRequest request) {
        Mate mate = Mate.builder()
                .user(savedUser)
                .region(Region.valueOf(request.getRegion()))
                .career(request.getCareer())
                .accommodatableDogsCount(request.getAccommodatableDogsCount())
                .build();


        return mateRepository.save(mate);

    }

    private Mate updatedMate(User savedUser, UpdateMateRequest request) {
        Mate mate = Mate.builder()
                .user(savedUser)
                .region(Region.valueOf(request.getRegion()))
                .career(request.getCareer())
                .accommodatableDogsCount(request.getAccommodatableDogsCount())
                .build();


        return mateRepository.save(mate);

    }

    private void saveMatePreferences(Mate savedMate, RegistMateRequest request) {

        Set<MateTag> mateTags = request.getPreferredStyle().stream().map(
                tag -> MateTag.builder()
                        .tagName(tag)
                        .mate(savedMate)
                        .build()
        ).collect(Collectors.toSet());

        mateTagRepository.saveAll(mateTags);


        Set<MatePreferredBreed> preferredBreeds = request.getPreferredBreed().stream()
                .map(breed -> MatePreferredBreed.builder()
                        .mate(savedMate)
                        .preferredBreed(Breed.valueOf(breed))
                        .build()
                )
                .collect(Collectors.toSet());


        Set<MatePreferredTime> preferredTimes = request.getPreferredTime().stream()
                .map(time -> MatePreferredTime.builder()
                        .mate(savedMate)
                        .preferredTime(Time.valueOf(time))
                        .build())
                .collect(Collectors.toSet());


        Set<MatePreferredWeek> preferredWeeks = request.getPreferredWeek().stream()
                .map(week -> MatePreferredWeek.builder()
                        .mate(savedMate)
                        .preferredWeek(Week.valueOf(week))
                        .build())
                .collect(Collectors.toSet());

        matePreferredBreedRepository.saveAll(preferredBreeds);
        matePreferredTimeRepository.saveAll(preferredTimes);
        matePreferredWeekRepository.saveAll(preferredWeeks);

        Mate updatedMate = savedMate.toBuilder()
                .mateTag(mateTags)
                .preferredBreed(preferredBreeds)
                .preferredTimes(preferredTimes)
                .preferredWeeks(preferredWeeks)
                .build();

        mateRepository.save(updatedMate);

    }

    private void saveMatePreferences(Mate savedMate, UpdateMateRequest request) {

        Set<MateTag> mateTags = request.getPreferredStyle().stream().map(
                tag -> MateTag.builder()
                        .tagName(tag)
                        .mate(savedMate)
                        .build()
        ).collect(Collectors.toSet());

        mateTagRepository.saveAll(mateTags);


        Set<MatePreferredBreed> preferredBreeds = request.getPreferredBreed().stream()
                .map(breed -> MatePreferredBreed.builder()
                        .mate(savedMate)
                        .preferredBreed(Breed.valueOf(breed))
                        .build()
                )
                .collect(Collectors.toSet());


        Set<MatePreferredTime> preferredTimes = request.getPreferredTime().stream()
                .map(time -> MatePreferredTime.builder()
                        .mate(savedMate)
                        .preferredTime(Time.valueOf(time))
                        .build())
                .collect(Collectors.toSet());


        Set<MatePreferredWeek> preferredWeeks = request.getPreferredWeek().stream()
                .map(week -> MatePreferredWeek.builder()
                        .mate(savedMate)
                        .preferredWeek(Week.valueOf(week))
                        .build())
                .collect(Collectors.toSet());

        matePreferredBreedRepository.saveAll(preferredBreeds);
        matePreferredTimeRepository.saveAll(preferredTimes);
        matePreferredWeekRepository.saveAll(preferredWeeks);

        Mate updatedMate = savedMate.toBuilder()
                .mateTag(mateTags)
                .preferredBreed(preferredBreeds)
                .preferredTimes(preferredTimes)
                .preferredWeeks(preferredWeeks)
                .build();

        mateRepository.save(updatedMate);

    }


}

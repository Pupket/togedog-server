package pupket.togedogserver.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;
import pupket.togedogserver.domain.user.entity.mate.Mate;
import pupket.togedogserver.domain.user.entity.mate.MateTag;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Builder
@Data
public class FindMateResponse {

    private long uuid;
    private long mateId;
    private String nickname;
    private String gender;
    private int age;
    private String birth;
    PreferredDetailsResponse preferred;
    private String profileImage;
    private int accommodatableDogsCount;
    private String career;

    public static FindMateResponse to(Mate findMate) {
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
                .preferred(getPreferredDetailsResponse(findMate))
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
}

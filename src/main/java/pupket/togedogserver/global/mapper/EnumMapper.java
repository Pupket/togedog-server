package pupket.togedogserver.global.mapper;

import org.mapstruct.Named;
import pupket.togedogserver.domain.board.constant.FeeType;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.user.constant.Region;
import pupket.togedogserver.domain.user.constant.Time;
import pupket.togedogserver.domain.user.constant.UserGender;
import pupket.togedogserver.domain.user.constant.Week;

public class EnumMapper {

    @Named("enumToKorean")
    public static String enumToKorean(Object enumValue) {
        if (enumValue == null) {
            return null;
        }

        if (enumValue instanceof UserGender) {
            return ((UserGender) enumValue).getGender();
        } else if (enumValue instanceof Region) {
            return ((Region) enumValue).getRegion();
        } else if (enumValue instanceof Time) {
            return ((Time) enumValue).getTime();
        } else if (enumValue instanceof Week) {
            return ((Week) enumValue).getWeek();
        } else if (enumValue instanceof DogType) {
            return ((DogType) enumValue).getBreed();
        } else if (enumValue instanceof Breed) {
            return ((Breed) enumValue).getType();
        } else if (enumValue instanceof FeeType) {
            return ((FeeType) enumValue).getFeeType();
        } else {
            return enumValue.toString(); // 기본적으로 enum 이름을 반환
        }
    }
}

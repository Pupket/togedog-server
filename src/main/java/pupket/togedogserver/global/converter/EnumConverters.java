package pupket.togedogserver.global.converter;

import pupket.togedogserver.domain.board.constant.FeeType;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.user.constant.*;

public class EnumConverters {
    public static final GenericEnumEditor.StringToEnumConverter<FeeType> FEE_TYPE_CONVERTER = FeeType::nameOf;
    public static final GenericEnumEditor.StringToEnumConverter<Breed> BREED_CONVERTER = Breed::nameOf;
    public static final GenericEnumEditor.StringToEnumConverter<DogType> DOG_TYPE_CONVERTER = DogType::nameOf;
    public static final GenericEnumEditor.StringToEnumConverter<AccountStatus> ACCOUNT_STATUS_CONVERTER = AccountStatus::nameOf;
    public static final GenericEnumEditor.StringToEnumConverter<Region> REGION_CONVERTER = Region::nameOf;
    public static final GenericEnumEditor.StringToEnumConverter<Time> TIME_CONVERTER = Time::nameOf;
    public static final GenericEnumEditor.StringToEnumConverter<UserGender> USER_GENDER_CONVERTER = UserGender::nameOf;
    public static final GenericEnumEditor.StringToEnumConverter<Week> WEEK_CONVERTER = Week::nameOf;
}
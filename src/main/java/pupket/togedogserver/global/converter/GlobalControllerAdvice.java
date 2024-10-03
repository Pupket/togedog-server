package pupket.togedogserver.global.converter;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.MultipartFile;
import pupket.togedogserver.domain.board.constant.FeeType;
import pupket.togedogserver.domain.dog.constant.Breed;
import pupket.togedogserver.domain.dog.constant.DogType;
import pupket.togedogserver.domain.user.constant.*;
import pupket.togedogserver.domain.user.dto.request.Preferred;

import java.beans.PropertyEditorSupport;

@ControllerAdvice
public class GlobalControllerAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(FeeType.class, new GenericEnumEditor<>(FeeType.class, EnumConverters.FEE_TYPE_CONVERTER));
        binder.registerCustomEditor(Breed.class, new GenericEnumEditor<>(Breed.class, EnumConverters.BREED_CONVERTER));
        binder.registerCustomEditor(DogType.class, new GenericEnumEditor<>(DogType.class, EnumConverters.DOG_TYPE_CONVERTER));
        binder.registerCustomEditor(AccountStatus.class, new GenericEnumEditor<>(AccountStatus.class, EnumConverters.ACCOUNT_STATUS_CONVERTER));
        binder.registerCustomEditor(Region.class, new GenericEnumEditor<>(Region.class, EnumConverters.REGION_CONVERTER));
        binder.registerCustomEditor(Time.class, new GenericEnumEditor<>(Time.class, EnumConverters.TIME_CONVERTER));
        binder.registerCustomEditor(UserGender.class, new GenericEnumEditor<>(UserGender.class, EnumConverters.USER_GENDER_CONVERTER));
        binder.registerCustomEditor(Week.class, new GenericEnumEditor<>(Week.class, EnumConverters.WEEK_CONVERTER));
        binder.registerCustomEditor(Preferred.class, new PreferredEditor());

        binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(null);
            }
        });
    }
}
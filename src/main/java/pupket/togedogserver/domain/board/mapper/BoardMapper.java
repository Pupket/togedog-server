package pupket.togedogserver.domain.board.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pupket.togedogserver.domain.board.dto.request.BoardCreateRequest;
import pupket.togedogserver.domain.board.dto.request.BoardUpdateRequest;
import pupket.togedogserver.domain.board.dto.response.BoardFindResponse;
import pupket.togedogserver.domain.board.entity.Board;
import pupket.togedogserver.domain.board.entity.WalkingPlaceTag;
import pupket.togedogserver.domain.dog.entity.Dog;
import pupket.togedogserver.global.mapper.EnumMapper;

import java.time.LocalTime;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BoardMapper {


    @Mapping(target = "boardId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "matched", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "chatRoom", ignore = true)
    @Mapping(target = "startTime", source = "startTime", dateFormat = "HH:mm:ss")
    @Mapping(target = "endTime", source = "endTime", dateFormat = "HH:mm:ss")
    @Mapping(target = "pickUpDay", source = "pickUpDay", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "dog", ignore = true)
    @Mapping(target = "walkingPlaceTag", ignore = true)
    Board toBoard(BoardCreateRequest boardCreateRequest);

    @Mapping(target = "boardId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "matched", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "chatRoom", ignore = true)
    @Mapping(target = "startTime", source = "startTime", dateFormat = "HH:mm:ss")
    @Mapping(target = "endTime", source = "endTime", dateFormat = "HH:mm:ss")
    @Mapping(target = "pickUpDay", source = "pickUpDay", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "dog", ignore = true)
    @Mapping(target = "walkingPlaceTag", ignore = true)
    Board toBoard(BoardUpdateRequest boardCreateRequest);


    @Mapping(target = "title", source = "board.title")
    @Mapping(target = "pickUpDay", source = "board.pickUpDay")
    @Mapping(target = "fee", source = "board.fee")
    @Mapping(target = "startTime", source = "board.startTime")
    @Mapping(target = "endTime", source = "board.endTime")
    @Mapping(target = "pickupLocation2", source = "board.pickupLocation2")
    @Mapping(target = "walkingPlaceTag", ignore = true)
    @Mapping(target = "name", source = "dog.name")
    @Mapping(target = "dogType", source = "dog.dogType")
    @Mapping(target= "feeType", source = "board.feeType")
    BoardFindResponse toResponse(Board board, Dog dog);

    @AfterMapping
    default void afterMapping(@MappingTarget BoardFindResponse response, Board board, Dog dog) {
        response.setDogType(EnumMapper.enumToKorean(dog.getBreed()));
        response.setFeeType(EnumMapper.enumToKorean(board.getFeeType()));

        if (board.getWalkingPlaceTag() != null) {
            response.setWalkingPlaceTag(board.getWalkingPlaceTag().stream()
                    .map(WalkingPlaceTag::getPlaceName)
                    .collect(Collectors.toList()));
        }
    }
}

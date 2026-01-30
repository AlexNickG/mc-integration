package ru.skillbox.socialnetwork.integration.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.skillbox.socialnetwork.integration.api.model.AreaModel;
import ru.skillbox.socialnetwork.integration.dto.CityDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CityMapper {
    @Mapping(target = "title", source = "name")
    @Mapping(target = "countryId", source = "parentId")
    CityDto hhApiToCityDto(AreaModel areaModel);
}

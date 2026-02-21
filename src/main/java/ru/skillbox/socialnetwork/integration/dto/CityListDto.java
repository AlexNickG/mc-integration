package ru.skillbox.socialnetwork.integration.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CityListDto {
    List<CityDto> cityDtoList = new ArrayList<>();
}

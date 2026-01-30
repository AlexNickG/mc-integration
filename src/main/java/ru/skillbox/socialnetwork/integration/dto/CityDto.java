package ru.skillbox.socialnetwork.integration.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class CityDto {

    private Integer id;
    private String title;
    private Integer countryId;
}

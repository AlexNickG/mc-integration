package ru.skillbox.socialnetwork.integration.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CountryDto {

    private Integer id;
    private String title;
    private List<CityDto> cities = new ArrayList<>();

}

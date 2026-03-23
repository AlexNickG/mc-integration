package ru.skillbox.socialnetwork.integration.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CountryDto implements Serializable {

    private Integer id;
    private String title;
    private List<CityDto> cities = new ArrayList<>();

}

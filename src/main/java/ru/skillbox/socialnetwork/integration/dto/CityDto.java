package ru.skillbox.socialnetwork.integration.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CityDto implements Serializable {

    private Integer id;
    private String title;
    private Integer countryId;
}

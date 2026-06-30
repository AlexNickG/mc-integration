package ru.skillbox.socialnetwork.integration.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AreaModel {
    private String id;
    private String name;
    private Float lat;
    private Float lng;
    @JsonProperty("parent_id")
    private String parentId;
    private List<AreaModel> subAreas = new ArrayList<>();
}

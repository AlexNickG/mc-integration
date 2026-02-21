package ru.skillbox.socialnetwork.integration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.skillbox.socialnetwork.integration.api.model.AreaModel;
import ru.skillbox.socialnetwork.integration.dto.CityDto;
import ru.skillbox.socialnetwork.integration.dto.CityListDto;
import ru.skillbox.socialnetwork.integration.dto.CountryDto;
import ru.skillbox.socialnetwork.integration.api.model.CountryModel;
import ru.skillbox.socialnetwork.integration.api.HhApiClient;
import ru.skillbox.socialnetwork.integration.mapper.CityMapper;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LocationService {

    private final HhApiClient apiClient;
    private final CityMapper cityMapper;

    @Cacheable("hh-countries")
    public List<CountryDto> getAllCountries() {
        List<CountryDto> countryDtoList = new ArrayList<>();
        List<CountryModel> allCountries = apiClient.getAllCountries();
        for (CountryModel country: allCountries) {
            CountryDto countryDto = new CountryDto();
            Integer countryId = Integer.valueOf(country.getId());
            countryDto.setId(countryId);
            countryDto.setTitle(country.getName());
            countryDto.setCities(getAllCitiesForCountry(countryId));
            countryDtoList.add(countryDto);
        }
        return countryDtoList;
    }

    @Cacheable("hh-cities")
    public CityListDto getAllCitiesForCountry1(Integer countryId) {
        List<CityDto> allCitiesForCountry = new ArrayList<>();
        AreaModel areaModel = apiClient.getCountryArea(countryId);
        List<AreaModel> areaModelList = areaModel.getAreas();//TODO: add NULL check
        for(AreaModel subAreaModel: areaModelList) {
           parseAreas(subAreaModel, countryId, allCitiesForCountry);
        }
        CityListDto response = new CityListDto();
        response.setCityDtoList(allCitiesForCountry);
        return response;
    }

    @Cacheable("hh-cities")
    public List<CityDto> getAllCitiesForCountry(Integer countryId) {
        List<CityDto> allCitiesForCountry = new ArrayList<>();
        AreaModel areaModel = apiClient.getCountryArea(countryId);
        List<AreaModel> areaModelList = areaModel.getAreas();//TODO: add NULL check
        for(AreaModel subAreaModel: areaModelList) {
            parseAreas(subAreaModel, countryId, allCitiesForCountry);
        }

        return allCitiesForCountry;
    }

    private void parseAreas(AreaModel area, Integer countryId, List<CityDto> allCitiesForCountry) {
        if (area == null) {
            return;
        }
        //List<AreaModel> subAreas = area.getAreas();
        if (area.getAreas().isEmpty() && area.getParentId() != null) {// если регион не имеет подрегионов, то это - город (необязательно! это может быть страна или регион без подрегионов)
            area.setParentId(String.valueOf(countryId));
            allCitiesForCountry.add(cityMapper.areaToCityDto(area));
        } else if (area.getParentId() != null) {// если регион имеет подрегионы, то рекурсивно идем до городов
            for (AreaModel subArea : area.getAreas()) {
                parseAreas(subArea, countryId, allCitiesForCountry);
            }
        }
    }
}

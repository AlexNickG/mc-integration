package ru.skillbox.socialnetwork.integration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.skillbox.socialnetwork.integration.api.HhApiClient;
import ru.skillbox.socialnetwork.integration.api.model.AreaModel;
import ru.skillbox.socialnetwork.integration.api.model.CountryModel;
import ru.skillbox.socialnetwork.integration.configuration.cache.AppCacheProperties;
import ru.skillbox.socialnetwork.integration.dto.CityDto;
import ru.skillbox.socialnetwork.integration.dto.CountryDto;
import ru.skillbox.socialnetwork.integration.mapper.CityMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@CacheConfig(cacheManager = "redisCacheManager")
public class LocationService {

    private final HhApiClient apiClient;
    private final CityMapper cityMapper;

    @Cacheable(value = AppCacheProperties.CacheNames.HH_COUNTRIES, sync = true)
    public List<CountryDto> getAllCountries() {
        List<CountryDto> countryDtoList = new ArrayList<>();
        List<CountryModel> allCountries = apiClient.getAllCountries();
        for (CountryModel country : allCountries) {
            int countryId;
            try{
                countryId = Integer.parseInt(country.getId());
            } catch (NumberFormatException e) {
                log.warn("Skipping country with invalid ID '{}' received from HH API", country.getId());
                continue;
            }
            CountryDto countryDto = new CountryDto();
            countryDto.setId(countryId);
            countryDto.setTitle(country.getName());
            countryDtoList.add(countryDto);
        }
        return countryDtoList;
    }

    @Cacheable(value = AppCacheProperties.CacheNames.HH_CITIES, sync = true)
    public List<CityDto> getAllCitiesForCountry(Integer countryId) {
        List<CityDto> allCitiesForCountry = new ArrayList<>();
        AreaModel areaModel = apiClient.getCountryArea(countryId);
        if (areaModel == null || areaModel.getSubAreas() == null) {
            return allCitiesForCountry;
        }
        for (AreaModel subAreaModel : areaModel.getSubAreas()) {
            parseAreas(subAreaModel, countryId, allCitiesForCountry);
        }
        return allCitiesForCountry;
    }

    private void parseAreas(AreaModel area, Integer countryId, List<CityDto> allCitiesForCountry) {
        if (area == null) {
            return;
        }
        List<AreaModel> subAreas = area.getSubAreas();
        if (subAreas == null || subAreas.isEmpty()) {
            // если регион не имеет подрегионов — это город, страна или регион без подрегионов
            if (area.getParentId() != null) {
                CityDto cityDto = cityMapper.areaToCityDto(area);
                cityDto.setCountryId(countryId);
                allCitiesForCountry.add(cityDto);
            }
        } else if (area.getParentId() != null) {
            // если регион имеет подрегионы — рекурсивно идем до городов
            for (AreaModel subArea : subAreas) {
                parseAreas(subArea, countryId, allCitiesForCountry);
            }
        }
    }
}

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

//    @Lazy
//    @Autowired
//    private LocationService self;

    @Cacheable(AppCacheProperties.CacheNames.HH_COUNTRIES)
    public List<CountryDto> getAllCountries() {
        List<CountryDto> countryDtoList = new ArrayList<>();
        List<CountryModel> allCountries = apiClient.getAllCountries();
        for (CountryModel country : allCountries) {
            CountryDto countryDto = new CountryDto();
            try{
                countryDto.setId(Integer.parseInt(country.getId()));
            } catch (NumberFormatException e) {
                log.info(e.getMessage());
               //throw new IllegalArgumentException("Country ID is invalid");
            }
            countryDto.setTitle(country.getName());
            countryDtoList.add(countryDto);
        }
//        for (CountryModel country : allCountries) {
//            try {
//                Integer countryId = Integer.valueOf(country.getId());
//                CountryDto countryDto = new CountryDto();
//                countryDto.setId(countryId);
//                countryDto.setTitle(country.getName());
//                countryDto.setCities(self.getAllCitiesForCountry(countryId));
//                countryDtoList.add(countryDto);
//            } catch (NumberFormatException e) {
//                log.warn("Error in external data: '{}'", country.getId());
//            }
//        }
        return countryDtoList;
    }

    @Cacheable(AppCacheProperties.CacheNames.HH_CITIES)
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

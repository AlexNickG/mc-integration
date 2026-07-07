package ru.skillbox.socialnetwork.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.skillbox.socialnetwork.integration.AbstractTest;
import ru.skillbox.socialnetwork.integration.api.HhApiClient;
import ru.skillbox.socialnetwork.integration.api.model.AreaModel;
import ru.skillbox.socialnetwork.integration.api.model.CountryModel;
import ru.skillbox.socialnetwork.integration.configuration.cache.AppCacheProperties;
import ru.skillbox.socialnetwork.integration.dto.CityDto;
import ru.skillbox.socialnetwork.integration.dto.CountryDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LocationServiceCacheIntegrationTest extends AbstractTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager cacheManager;

    @MockitoBean
    private HhApiClient hhApiClient;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    void getAllCountries_secondCall_isServedFromCacheWithoutSecondHhApiCall() {
        when(hhApiClient.getAllCountries())
                .thenReturn(List.of(country("113", "Россия"), country("40", "Казахстан")));

        List<CountryDto> firstCall = locationService.getAllCountries();
        List<CountryDto> secondCall = locationService.getAllCountries();

        verify(hhApiClient, times(1)).getAllCountries();
        assertThat(firstCall).hasSize(2);
        assertThat(secondCall).usingRecursiveComparison().isEqualTo(firstCall);
    }

    @Test
    void getAllCounties_resultIsPhysicallyStoredInRedis() {
        when(hhApiClient.getAllCountries()).thenReturn(List.of(country("113", "Россия")));

        locationService.getAllCountries();

        Cache countriesCache = cacheManager.getCache(AppCacheProperties.CacheNames.HH_COUNTRIES);
        assertThat(countriesCache).isNotNull();
        assertThat(countriesCache.get(SimpleKey.EMPTY)).isNotNull();
    }

    @Test
    void getAllCitiesForCountry_IsCachedPerCountryId() {
        when(hhApiClient.getCountryArea(113))
                .thenReturn(countryArea("113", area("1", "Москва", "113")));
        when(hhApiClient.getCountryArea(40))
                .thenReturn(countryArea("40", area("159", "Астана", "40")));

        locationService.getAllCitiesForCountry(113);
        locationService.getAllCitiesForCountry(113);

        List<CityDto> kazakhstanCities = locationService.getAllCitiesForCountry(40);

        verify(hhApiClient, times(1)).getCountryArea(113);
        verify(hhApiClient, times(1)).getCountryArea(40);
        assertThat(kazakhstanCities).hasSize(1);
        assertThat(kazakhstanCities.get(0).getTitle()).isEqualTo("Астана");
        assertThat(kazakhstanCities.get(0).getCountryId()).isEqualTo(40);
    }
    private CountryModel country(String id, String name) {
        CountryModel countryModel = new CountryModel();
        countryModel.setId(id);
        countryModel.setName(name);
        return countryModel;
    }

    private AreaModel countryArea(String id, AreaModel... children) {
        AreaModel root = new AreaModel();
        root.setId(id);
        root.getSubAreas().addAll(List.of(children));
        return root;
    }

    private AreaModel area(String id, String name, String parentId) {
        AreaModel areaModel = new AreaModel();
        areaModel.setId(id);
        areaModel.setName(name);
        areaModel.setParentId(parentId);
        return areaModel;
    }
}

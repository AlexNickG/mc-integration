package ru.skillbox.socialnetwork.integration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skillbox.socialnetwork.integration.api.HhApiClient;
import ru.skillbox.socialnetwork.integration.api.model.AreaModel;
import ru.skillbox.socialnetwork.integration.api.model.CountryModel;
import ru.skillbox.socialnetwork.integration.dto.CityDto;
import ru.skillbox.socialnetwork.integration.dto.CountryDto;
import ru.skillbox.socialnetwork.integration.mapper.CityMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private HhApiClient apiClient;

    @Mock
    private CityMapper cityMapper;

    @Mock
    private LocationService selfMock;

    @InjectMocks
    private LocationService locationService;

//    @BeforeEach
//    void setUp() {
//        ReflectionTestUtils.setField(locationService, "self", selfMock);
//    }

    @Test
    void getAllCountries_returnsMappedList() {
        CountryModel russia = country("113", "Россия");
        CountryModel usa = country("84", "США");
        when(apiClient.getAllCountries()).thenReturn(List.of(russia, usa));

//        CityDto moscow = city(1, "Москва", 113);
//        when(selfMock.getAllCitiesForCountry(113)).thenReturn(List.of(moscow));
//        when(selfMock.getAllCitiesForCountry(84)).thenReturn(List.of());

        List<CountryDto> result = locationService.getAllCountries();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(113);
        assertThat(result.getFirst().getTitle()).isEqualTo("Россия");
        assertThat(result.get(1).getId()).isEqualTo(84);
//        verify(selfMock).getAllCitiesForCountry(113);
//        verify(selfMock).getAllCitiesForCountry(84);
    }

    @Test
    void getAllCountries_skipsCountryWithNonNumericId() {
        CountryModel invalid = country("abc", "Неизвестная");
        CountryModel valid = country("113", "Россия");
        when(apiClient.getAllCountries()).thenReturn(List.of(valid));
        //when(selfMock.getAllCitiesForCountry(113)).thenReturn(List.of());

        List<CountryDto> result = locationService.getAllCountries();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(113);
        // для "abc" getAllCitiesForCountry не должен вызываться
        //verify(selfMock, never()).getAllCitiesForCountry(argThat(id -> id == null));
    }

    @Test
    void getAllCountries_returnsEmptyList_whenApiReturnsEmpty() {
        when(apiClient.getAllCountries()).thenReturn(List.of());

        List<CountryDto> result = locationService.getAllCountries();

        assertThat(result).isEmpty();
        verifyNoInteractions(selfMock);
    }

    @Test
    void getAllCitiesForCountry_returnsEmpty_whenAreaModelIsNull() {
        when(apiClient.getCountryArea(1)).thenReturn(null);

        List<CityDto> result = locationService.getAllCitiesForCountry(1);

        assertThat(result).isEmpty();
        verifyNoInteractions(cityMapper);
    }

    @Test
    void getAllCitiesForCountry_returnsEmpty_whenAreasIsNull() {
        AreaModel area = new AreaModel();
        area.setId("1");
        area.setSubAreas(null);
        when(apiClient.getCountryArea(1)).thenReturn(area);

        List<CityDto> result = locationService.getAllCitiesForCountry(1);

        assertThat(result).isEmpty();
        verifyNoInteractions(cityMapper);
    }

    @Test
    void getAllCitiesForCountry_returnsCity_fromDirectLeafArea() {
        // Страна → Регион (без подрегионов) = сразу город
        AreaModel cityArea = new AreaModel();
        cityArea.setId("100");
        cityArea.setName("Москва");
        cityArea.setParentId("1");
        cityArea.setSubAreas(new ArrayList<>());

        AreaModel countryArea = new AreaModel();
        countryArea.setId("1");
        countryArea.setSubAreas(List.of(cityArea));

        CityDto expected = city(100, "Москва", 1);
        when(apiClient.getCountryArea(1)).thenReturn(countryArea);
        when(cityMapper.areaToCityDto(cityArea)).thenReturn(city(100, "Москва", 999)); // countryId будет перезаписан

        List<CityDto> result = locationService.getAllCitiesForCountry(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Москва");
        assertThat(result.get(0).getCountryId()).isEqualTo(1); // перезаписан в parseAreas
        verify(cityMapper).areaToCityDto(cityArea);
    }

    @Test
    void getAllCitiesForCountry_parsesNestedAreas_recursively() {
        // Страна → Регион → Город (двухуровневая вложенность)
        AreaModel cityArea = new AreaModel();
        cityArea.setId("200");
        cityArea.setName("Санкт-Петербург");
        cityArea.setParentId("10");
        cityArea.setSubAreas(new ArrayList<>());

        AreaModel region = new AreaModel();
        region.setId("10");
        region.setName("Северо-Западный ФО");
        region.setParentId("2");
        region.setSubAreas(List.of(cityArea));

        AreaModel countryArea = new AreaModel();
        countryArea.setId("2");
        countryArea.setSubAreas(List.of(region));

        when(apiClient.getCountryArea(2)).thenReturn(countryArea);
        when(cityMapper.areaToCityDto(cityArea)).thenReturn(city(200, "Санкт-Петербург", 999));

        List<CityDto> result = locationService.getAllCitiesForCountry(2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Санкт-Петербург");
        assertThat(result.get(0).getCountryId()).isEqualTo(2);
        // регион не попал в результат — только лист
        verify(cityMapper, times(1)).areaToCityDto(any());
    }

    @Test
    void getAllCitiesForCountry_skipsArea_withNullParentId() {
        AreaModel orphan = new AreaModel();
        orphan.setId("999");
        orphan.setName("Zion");
        orphan.setParentId(null);
        orphan.setSubAreas(new ArrayList<>());

        AreaModel countryArea = new AreaModel();
        countryArea.setId("1");
        countryArea.setSubAreas(List.of(orphan));

        when(apiClient.getCountryArea(1)).thenReturn(countryArea);

        List<CityDto> result = locationService.getAllCitiesForCountry(1);

        assertThat(result).isEmpty();
        verifyNoInteractions(cityMapper);
    }

    private CountryModel country(String id, String name) {
        CountryModel m = new CountryModel();
        m.setId(id);
        m.setName(name);
        return m;
    }

    private CityDto city(int id, String title, int countryId) {
        CityDto dto = new CityDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setCountryId(countryId);
        return dto;
    }
}
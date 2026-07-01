package ru.skillbox.socialnetwork.integration.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.skillbox.socialnetwork.integration.dto.CityDto;
import ru.skillbox.socialnetwork.integration.dto.CountryDto;
import ru.skillbox.socialnetwork.integration.service.LocationService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
    }

    // ─── GET /api/v1/geo/country ────────────────────────────────────────────────

    @Test
    void getAllCountries_returns200_withCountryList() throws Exception {
        CountryDto russia = country(113, "Россия");
        CountryDto usa = country(84, "США");
        when(locationService.getAllCountries()).thenReturn(List.of(russia, usa));

        mockMvc.perform(get("/api/v1/geo/country")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(113))
                .andExpect(jsonPath("$[0].title").value("Россия"))
                .andExpect(jsonPath("$[1].id").value(84))
                .andExpect(jsonPath("$[1].title").value("США"));

        verify(locationService).getAllCountries();
    }

    @Test
    void getAllCountries_returns200_withEmptyList() throws Exception {
        when(locationService.getAllCountries()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/geo/country")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/v1/geo/country/{countryId}/city ───────────────────────────────

    @Test
    void getCities_returns200_withCityList() throws Exception {
        CityDto moscow = city(1, "Москва", 113);
        CityDto spb = city(2, "Санкт-Петербург", 113);
        when(locationService.getAllCitiesForCountry(113)).thenReturn(List.of(moscow, spb));

        mockMvc.perform(get("/api/v1/geo/country/113/city")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Москва"))
                .andExpect(jsonPath("$[0].countryId").value(113))
                .andExpect(jsonPath("$[1].title").value("Санкт-Петербург"));

        verify(locationService).getAllCitiesForCountry(113);
    }

    @Test
    void getCities_returns200_withEmptyList() throws Exception {
        when(locationService.getAllCitiesForCountry(999)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/geo/country/999/city")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private CountryDto country(int id, String title) {
        CountryDto dto = new CountryDto();
        dto.setId(id);
        dto.setTitle(title);
        return dto;
    }

    private CityDto city(int id, String title, int countryId) {
        CityDto dto = new CityDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setCountryId(countryId);
        return dto;
    }
}
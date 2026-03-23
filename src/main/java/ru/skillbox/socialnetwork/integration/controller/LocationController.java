package ru.skillbox.socialnetwork.integration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.socialnetwork.integration.dto.CityDto;
import ru.skillbox.socialnetwork.integration.dto.CountryDto;
import ru.skillbox.socialnetwork.integration.service.LocationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/geo")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/country")
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        return ResponseEntity.ok(locationService.getAllCountries());
    }

    @GetMapping("/country/{countryId}/city")
    public ResponseEntity<List<CityDto>> getCities(@PathVariable Integer countryId) {
        return ResponseEntity.ok(locationService.getAllCitiesForCountry(countryId));
    }

}

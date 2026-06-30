package ru.skillbox.socialnetwork.integration.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skillbox.socialnetwork.integration.dto.CityDto;
import ru.skillbox.socialnetwork.integration.dto.CountryDto;
import ru.skillbox.socialnetwork.integration.service.LocationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/geo")
@Validated
@Tag(name = "Geo API v.1", description = "Client API version v.1")
public class LocationController {

    private final LocationService locationService;

    @Operation(
            summary = "Get all countries",
            description = "Return list of all countries "
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Countries found",
                    content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CountryDto.class))
                    )
            ),
            @ApiResponse(responseCode = "502", description = "HH API is unavailable")
    })
    @GetMapping("/country")
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        return ResponseEntity.ok(locationService.getAllCountries());
    }

    @Operation(
            summary = "Get cities by country ID",
            description = "Returns cities for the specified country"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cities found",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CityDto.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid country ID"),
            @ApiResponse(responseCode = "502", description = "HH API is unavailable")
    })
    @GetMapping("/country/{countryId}/city")
    public ResponseEntity<List<CityDto>> getCities(
            @Parameter(description = "Country ID", example = "113", required = true)
            @PathVariable("countryId")
            @Min(value = 1, message = "countryId can't be low than 1")
            Integer countryId) {
        return ResponseEntity.ok(locationService.getAllCitiesForCountry(countryId));
    }
}

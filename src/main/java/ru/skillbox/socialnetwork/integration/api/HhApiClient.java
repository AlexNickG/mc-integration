package ru.skillbox.socialnetwork.integration.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.skillbox.socialnetwork.integration.api.model.AreaModel;
import ru.skillbox.socialnetwork.integration.api.model.CountryModel;

import java.util.List;

@FeignClient(name = "hhApiClient", url = "${hh.api.base-url}")
public interface HhApiClient {

    @GetMapping(value = "/areas/countries")
    List<CountryModel> getAllCountries();

    @GetMapping(value = "/areas/{id}")
    AreaModel getCountryArea(@PathVariable("id") Integer id);

}

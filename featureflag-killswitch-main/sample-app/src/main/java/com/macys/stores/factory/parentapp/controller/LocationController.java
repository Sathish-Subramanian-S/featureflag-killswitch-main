package com.macys.stores.factory.parentapp.controller;

import com.macys.stores.factory.parentapp.service.LocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/locations/{locationId}")
    public Mono<String> getLocation(@PathVariable("locationId") String locationId, @RequestHeader("X-User-Key") String userKey) {
        return locationService.getLocationByIdAsString(locationId, userKey);
    }
}

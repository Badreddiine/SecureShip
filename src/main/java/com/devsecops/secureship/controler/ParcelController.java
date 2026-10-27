package com.devsecops.secureship.controler;

import com.devsecops.secureship.dto.CreateParcelRequest;
import com.devsecops.secureship.dto.ParcelResponse;
import com.devsecops.secureship.service.CacheResult;
import com.devsecops.secureship.service.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService service;

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> getById(@PathVariable("id") Long id) {
        CacheResult<ParcelResponse> result = service.findById(id);
        return ResponseEntity.ok()
                .header("X-Cache", result.hit() ? "HIT" : "MISS")
                .body(result.value());
    }

    // Needed to put data in the database so GET /parcels/{id} can be tested
    @PostMapping
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody CreateParcelRequest request) {
        ParcelResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/parcels/" + created.id())).body(created);
    }
}
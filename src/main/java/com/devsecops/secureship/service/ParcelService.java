package com.devsecops.secureship.service;


import com.devsecops.secureship.dto.CreateParcelRequest;
import com.devsecops.secureship.dto.ParcelResponse;
import com.devsecops.secureship.entity.Parcel;
import com.devsecops.secureship.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParcelService {

    private static final String KEY_PREFIX = "parcel:";

    private final ParcelRepository repository;
    private final StringRedisTemplate redis;

    @Value("${app.cache.parcel-ttl:60s}")
    private Duration ttl;

    /** Cache-aside: Redis first, PostgreSQL on a miss, then fill Redis. */
    public CacheResult<ParcelResponse> findById(Long id) {
        String key = KEY_PREFIX + id;

        ParcelResponse cached = readFromCache(key);
        if (cached != null) {
            return new CacheResult<>(cached, true);
        }

        Parcel parcel = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Parcel " + id + " not found"));

        ParcelResponse response = ParcelResponse.from(parcel);
        writeToCache(key, response);
        return new CacheResult<>(response, false);
    }

    public ParcelResponse create(CreateParcelRequest request) {
        Parcel saved = repository.save(new Parcel(
                null, request.trackingNumber(), request.status(), request.destination()));
        return ParcelResponse.from(saved);
    }

    private ParcelResponse readFromCache(String key) {
        try {
            HashOperations<String, String, String> ops = redis.opsForHash();
            Map<String, String> fields = ops.entries(key);
            if (fields.isEmpty()) {
                return null;
            }
            return new ParcelResponse(
                    Long.valueOf(fields.get("id")),
                    fields.get("trackingNumber"),
                    fields.get("status"),
                    fields.get("destination"));
        } catch (DataAccessException | NumberFormatException e) {
            // Redis down or corrupted entry: fall back to the database
            log.warn("Cache read failed for {}: {}", key, e.getMessage());
            return null;
        }
    }

    private void writeToCache(String key, ParcelResponse parcel) {
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("id", String.valueOf(parcel.id()));
            fields.put("trackingNumber", parcel.trackingNumber());
            fields.put("status", parcel.status());
            fields.put("destination", parcel.destination());

            redis.opsForHash().putAll(key, fields);
            redis.expire(key, ttl);
        } catch (DataAccessException e) {
            log.warn("Cache write failed for {}: {}", key, e.getMessage());
        }
    }
}

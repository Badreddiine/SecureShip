package com.devsecops.secureship.service;


/** A value plus whether it came from the cache (HIT) or from the database (MISS). */
public record CacheResult<T>(T value, boolean hit) {
}

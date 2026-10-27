package com.devsecops.secureship.repository;


import com.devsecops.secureship.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
}

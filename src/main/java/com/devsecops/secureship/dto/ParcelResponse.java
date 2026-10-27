package com.devsecops.secureship.dto;


import com.devsecops.secureship.entity.Parcel;

public record ParcelResponse(Long id, String trackingNumber, String status, String destination) {

    public static ParcelResponse from(Parcel parcel) {
        return new ParcelResponse(
                parcel.getId(),
                parcel.getTrackingNumber(),
                parcel.getStatus(),
                parcel.getDestination());
    }
}

package com.devsecops.secureship.dto;


import jakarta.validation.constraints.NotBlank;

public record CreateParcelRequest(
        @NotBlank String trackingNumber,
        @NotBlank String status,
        @NotBlank String destination) {
}

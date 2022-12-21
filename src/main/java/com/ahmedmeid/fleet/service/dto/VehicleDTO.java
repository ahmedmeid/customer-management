package com.ahmedmeid.fleet.service.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDTO {

    private UUID uuid;

    private String vehicleId;

    private String vehicleRegNo;

    private String deviceId;
}

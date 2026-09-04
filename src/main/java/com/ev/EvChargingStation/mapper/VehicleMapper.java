package com.ev.EvChargingStation.mapper;

import com.ev.EvChargingStation.dto.vehicle.VehicleResponseDTO;
import com.ev.EvChargingStation.entity.Vehicle;
import com.ev.EvChargingStation.entity.VehicleCatalogue;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    default VehicleResponseDTO entityToDto(Vehicle vehicle) {
        VehicleCatalogue c = vehicle.getCatalogueVehicle();
        return new VehicleResponseDTO(
                vehicle.getId(),
                c.getId(),
                c.getManufacturer(),
                c.getModel(),
                c.getVariant(),
                vehicle.getRegistrationNumber(),
                c.getBatteryCapacityKwh(),
                c.getConnectorType(),
                c.getMaxAcChargingKw(),
                c.getMaxDcChargingKw(),
                c.getImagePath()
        );
    }
}

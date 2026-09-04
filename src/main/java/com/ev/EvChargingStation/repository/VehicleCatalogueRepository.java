package com.ev.EvChargingStation.repository;

import com.ev.EvChargingStation.entity.VehicleCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleCatalogueRepository extends JpaRepository<VehicleCatalogue, Long> {
    List<VehicleCatalogue> findByManufacturerIgnoreCaseOrderByModelAscVariantAsc(String manufacturer);
    List<VehicleCatalogue> findByManufacturerIgnoreCaseAndModelIgnoreCaseOrderByVariantAsc(String manufacturer, String model);
}

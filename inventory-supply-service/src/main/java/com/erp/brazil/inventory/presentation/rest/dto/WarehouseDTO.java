package com.erp.brazil.inventory.presentation.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDTO {
    private UUID id;
    private UUID companyId;
    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Boolean isActive;
    private Boolean isDefault;
    private Boolean allowNegativeStock;
    private List<LocationDTO> locations;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class LocationDTO {
    private UUID id;
    private String code;
    private String name;
    private String locationType;
    private String aisle;
    private String rack;
    private String shelf;
    private String bin;
    private Boolean isActive;
    private Boolean isPickable;
    private Boolean isReceivable;
}
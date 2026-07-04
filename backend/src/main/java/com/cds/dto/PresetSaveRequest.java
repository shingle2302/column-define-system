package com.cds.dto;

import lombok.Data;

@Data
public class PresetSaveRequest {
    private String name;
    private String description;
    private String systemId;
    private String businessId;
    private Boolean allowCrossGroup;
    private Boolean isDefault;
    private Integer minVisibleColumns;
    private Integer maxVisibleColumns;
}

package com.cds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PresetDetailResponse {
    private String id;
    private String name;
    private String description;
    private String systemId;
    private String systemName;
    private String businessId;
    private String businessName;
    private Boolean allowCrossGroup;
    private Boolean isDefault;
    private Integer minVisibleColumns;
    private Integer maxVisibleColumns;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

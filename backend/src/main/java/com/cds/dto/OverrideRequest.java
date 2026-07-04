package com.cds.dto;

import lombok.Data;

import java.util.Map;

@Data
public class OverrideRequest {
    private String presetId;
    private Map<String, ColumnOverrideItem> columnOverrides;
    private Integer version;
    private Long updatedAt;

    @Data
    public static class ColumnOverrideItem {
        private Integer order;
        private Integer width;
        private Boolean visible;
        private String pinned;
    }
}

package com.cds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverrideResponse {
    private String presetId;
    private Map<String, ColumnOverrideData> columnOverrides;
    private Integer version;
    private Long updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnOverrideData {
        private Integer order;
        private Integer width;
        private Boolean visible;
        private String pinned;
    }
}

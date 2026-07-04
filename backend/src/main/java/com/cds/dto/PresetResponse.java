package com.cds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresetResponse {
    private String id;
    private String name;
    private String description;
    private String systemId;
    private String businessId;
    private List<GroupNode> groups;
    private Boolean allowCrossGroup;
    private Integer version;
    private Integer minVisibleColumns;
    private Integer maxVisibleColumns;
    private Long createdAt;
    private Long updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupNode {
        private String id;
        private String label;
        private List<ColumnNode> children;
        private Boolean collapsed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnNode {
        private String id;
        private String label;
        private String field;
        private String type;
        private Integer width;
        private Integer minWidth;
        private Integer maxWidth;
        private Boolean sortable;
        private Boolean filterable;
        private Boolean resizable;
        private Boolean visible;
        private String pinned;
        private Boolean locked;
        private Boolean allowHide;
        private Boolean allowSort;
        private Boolean allowResize;
        private Boolean allowPin;
        private String align;
        private Object validation;
        private List<OptionNode> options;
        private Object defaultValue;
        private Map<String, Object> meta;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionNode {
        private String label;
        private String value;
    }
}

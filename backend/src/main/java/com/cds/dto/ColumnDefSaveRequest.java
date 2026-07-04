package com.cds.dto;

import lombok.Data;

@Data
public class ColumnDefSaveRequest {
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
    private String validationJson;
    private String optionsJson;
    private String defaultValue;
    private String metaJson;
    private Integer sortOrder;
}

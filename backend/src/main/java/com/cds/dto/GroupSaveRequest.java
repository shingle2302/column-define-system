package com.cds.dto;

import lombok.Data;

@Data
public class GroupSaveRequest {
    private String label;
    private String parentId;
    private Integer sortOrder;
    private Boolean collapsed;
}

package com.cds.dto;

import lombok.Data;

@Data
public class BusinessSaveRequest {
    private String systemId;
    private String name;
    private String code;
    private String description;
    private Integer sortOrder;
}

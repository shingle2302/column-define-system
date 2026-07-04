package com.cds.dto;

import lombok.Data;

@Data
public class SystemSaveRequest {
    private String name;
    private String code;
    private String description;
    private String status;
}

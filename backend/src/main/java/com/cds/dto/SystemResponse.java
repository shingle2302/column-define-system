package com.cds.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemResponse {
    private String id;
    private String name;
    private String code;
    private String description;
    private String status;
    private List<BusinessNode> businesses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessNode {
        private String id;
        private String name;
        private String code;
        private String description;
        private Integer sortOrder;
    }
}

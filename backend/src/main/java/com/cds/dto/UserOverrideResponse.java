package com.cds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** 管理后台用的用户覆盖列表项 */
@Data
@AllArgsConstructor
public class UserOverrideResponse {
    private Long id;
    private String presetId;
    private String presetName;
    private String userId;
    private String userName;
    private String columnId;
    private String columnLabel;
    private String columnField;
    private Integer overrideOrder;
    private Integer overrideWidth;
    private Boolean overrideVisible;
    private String overridePinned;
    private Integer version;
    private LocalDateTime updatedAt;
}

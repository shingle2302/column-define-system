package com.cds.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统实体 — 代表一个独立的业务系统（如 CRM、HR、MKT 等）
 */
@Data
@TableName("cds_system")
public class CdsSystem {

    /** 主键 ID */
    @TableId
    private String id;

    /** 系统名称（如：客户关系管理系统） */
    private String name;

    /** 系统编码（如：crm、hr、mkt，唯一标识） */
    private String code;

    /** 系统描述 */
    private String description;

    /** 状态：ACTIVE=启用, INACTIVE=停用 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}

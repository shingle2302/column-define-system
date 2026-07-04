package com.cds.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务实体 — 隶属于某个系统下的具体业务模块
 */
@Data
@TableName("cds_business")
public class CdsBusiness {

    /** 主键 ID */
    @TableId
    private String id;

    /** 所属系统的 ID */
    private String systemId;

    /** 业务名称（如：客户管理、营销活动） */
    private String name;

    /** 业务编码（如：cust_mgmt、mkt_campaign，在所属系统下唯一） */
    private String code;

    /** 业务描述 */
    private String description;

    /** 排序序号（越小越靠前） */
    private Integer sortOrder;

    /** 所属系统名称（非数据库字段，仅用于联表查询时的展示） */
    @TableField(exist = false)
    private String systemName;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}

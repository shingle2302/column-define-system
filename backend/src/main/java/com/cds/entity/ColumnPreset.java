package com.cds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 列预设实体 — 定义一套预设的列配置方案，供用户快速加载
 */
@Data
@TableName("column_preset")
public class ColumnPreset {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 预设名称（如：客户信息完整视图、客户信息精简视图） */
    private String name;

    /** 预设描述 */
    private String description;

    /** 所属系统的 ID */
    private String systemId;

    /** 所属业务的 ID */
    private String businessId;

    /** 是否允许跨分组拖拽排序 */
    private Boolean allowCrossGroup;

    /** 版本号（乐观锁，用于并发控制） */
    private Integer version;

    /** 是否为该业务的默认预设 */
    private Boolean isDefault;

    /** 最少可见列数（用户隐藏列时不得低于此值） */
    private Integer minVisibleColumns;

    /** 最多可见列数（用户显示列时不得高于此值） */
    private Integer maxVisibleColumns;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}

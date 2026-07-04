package com.cds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列覆盖实体 — 记录用户对预设列配置的个性化覆盖
 */
@Data
@TableName("column_override")
public class ColumnOverride {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属预设的 ID */
    private String presetId;

    /** 用户 ID */
    private String userId;

    /** 被覆盖的列定义 ID */
    private String columnId;

    /** 用户自定义的列排序（覆盖预设排序） */
    private Integer overrideOrder;

    /** 用户自定义的列宽（覆盖预设宽度） */
    private Integer overrideWidth;

    /** 用户自定义的列可见性（覆盖预设可见性） */
    private Boolean overrideVisible;

    /** 用户自定义的列固定位置（覆盖预设固定位置） */
    private String overridePinned;

    /** 版本号（乐观锁） */
    private Integer version;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}

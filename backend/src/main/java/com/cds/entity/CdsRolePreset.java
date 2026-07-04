package com.cds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色预设覆盖实体 — 按角色维度对预设中的列定义进行统一覆盖（优先级低于用户级覆盖）
 */
@Data
@TableName("cds_role_preset")
public class CdsRolePreset {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属预设的 ID */
    private String presetId;

    /** 角色标识（如：admin、viewer） */
    private String role;

    /** 被覆盖的列定义 ID */
    private String columnId;

    /** 按角色自定义的列排序 */
    private Integer overrideOrder;

    /** 按角色自定义的列宽 */
    private Integer overrideWidth;

    /** 按角色自定义的列可见性 */
    private Boolean overrideVisible;

    /** 按角色自定义的列固定位置 */
    private String overridePinned;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}

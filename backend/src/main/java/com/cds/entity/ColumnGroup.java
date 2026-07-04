package com.cds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 列分组实体 — 将列定义组织成有层级的树形分组结构
 */
@Data
@TableName("column_group")
public class ColumnGroup {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 分组显示名称（如：基本信息、联系方式） */
    private String label;

    /** 父分组 ID（null 表示顶层分组） */
    private String parentId;

    /** 所属预设的 ID */
    private String presetId;

    /** 是否默认折叠 */
    private Boolean collapsed;

    /** 排序序号（越小越靠前） */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    /** 该分组下的列定义列表（非数据库字段，仅用于嵌套查询返回） */
    @TableField(exist = false)
    private List<ColumnDefinition> columns;
}

package com.cds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 列定义实体 — 描述表格中单列的所有元数据信息
 *
 * <pre>{@code
 * validationJson 配置示例（JSON 数组，每项一条校验规则）
 * 前端支持的 type 仅四种：required | pattern | range | custom
 * [
 *   {"type": "required",  "message": "该项为必填"},
 *   {"type": "pattern",   "message": "仅允许字母数字下划线",          "params": {"regex": "^[a-zA-Z0-9_]+$"}},
 *   {"type": "pattern",   "message": "最少2个字符，最多50个字符",     "params": {"regex": "^.{2,50}$"}},
 *   {"type": "pattern",   "message": "请输入正确的邮箱格式",          "params": {"regex": "^\\S+@\\S+\\.\\S+$"}},
 *   {"type": "pattern",   "message": "请输入正确的11位手机号",        "params": {"regex": "^1[3-9]\\d{9}$"}},
 *   {"type": "range",     "message": "数值范围 0 ~ 999999",          "params": {"min": 0, "max": 999999}},
 *   {"type": "custom",    "message": "自定义校验（前端自行实现）"}
 * ]
 * }</pre>
 */
@Data
@TableName("column_definition")
public class ColumnDefinition {

    /** 主键 ID（雪花算法自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 列显示名称（中文表头） */
    private String label;

    /** 列字段名（对应数据源 field key） */
    private String field;

    /**
     * 列数据类型，可选值：
     * text / number / date / select / boolean / email / phone / url 等
     */
    private String type;

    /** 默认列宽（px） */
    private Integer width;

    /** 最小列宽（px） */
    private Integer minWidth;

    /** 最大列宽（px） */
    private Integer maxWidth;

    /** 是否支持排序 */
    private Boolean sortable;

    /** 是否支持筛选/过滤 */
    private Boolean filterable;

    /** 是否允许用户拖拽调整列宽 */
    private Boolean resizable;

    /** 默认是否可见 */
    private Boolean visible;

    /** 固定列位置：null=不固定, left=左固定, right=右固定 */
    private String pinned;

    /** 是否锁定（锁定后用户不可修改该列的任何配置） */
    private Boolean locked;

    /** 是否允许用户隐藏该列 */
    private Boolean allowHide;

    /** 是否允许用户对该列排序 */
    private Boolean allowSort;

    /** 是否允许用户调整该列宽度 */
    private Boolean allowResize;

    /** 是否允许用户固定该列 */
    private Boolean allowPin;

    /**
     * 文本对齐方式：left=居左, center=居中, right=居右，默认 left
     */
    private String align;

    /**
     * 校验规则 JSON 数组，格式见类级别 javadoc 示例。
     * 每个元素为 {type, value?, message} 的校验规则对象。
     */
    private String validationJson;

    /**
     * 下拉选项 JSON 数组（当 type=select 时生效），示例：
     * [{"label": "VIP客户", "value": "vip"}, {"label": "普通客户", "value": "normal"}]
     */
    private String optionsJson;

    /** 默认值 */
    private String defaultValue;

    /**
     * 扩展元数据 JSON 对象，可存放任意自定义配置，示例：
     * {"format": "yyyy-MM-dd", "align": "center", "tooltip": true}
     */
    private String metaJson;

    /** 所属分组的 ID */
    private String groupId;

    /** 在分组内的排序序号（越小越靠前） */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}

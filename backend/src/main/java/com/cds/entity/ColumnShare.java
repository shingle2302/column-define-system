package com.cds.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 列配置分享实体 — 用户可将自己的个性化列配置生成分享链接
 */
@Data
@TableName("column_share")
public class ColumnShare {

    /** 分享令牌（主键，用于生成分享链接的唯一标识） */
    @TableId
    private String token;

    /** 所属预设的 ID */
    private String presetId;

    /** 创建分享的用户 ID */
    private String userId;

    /** 覆盖数据 JSON（包含该用户对预设的所有覆盖项） */
    private String overrideData;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 过期时间（超过此时间分享链接失效） */
    private LocalDateTime expiresAt;
}

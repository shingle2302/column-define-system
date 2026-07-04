package com.cds.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 主键 ID */
    @TableId
    private String id;

    /** 登录用户名 */
    private String username;

    /** 登录密码（加密存储） */
    private String password;

    /** 用户显示名称（如：张三） */
    private String displayName;

    /** 用户角色：admin=管理员, user=普通用户 */
    private String role;

    /** 所属系统的 ID */
    private String systemId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

package com.cds.controller;

import com.cds.dto.*;
import com.cds.entity.*;
import com.cds.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== Auth helpers ====================

    private String getRole(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }

    private String getSystemId(HttpServletRequest request) {
        return (String) request.getAttribute("systemId");
    }

    private boolean isAdmin(String role) {
        return "admin".equals(role);
    }

    private boolean isEditor(String role) {
        return "admin".equals(role) || "user".equals(role);
    }

    private boolean canRead(String role) {
        return "admin".equals(role) || "user".equals(role) || "viewer".equals(role);
    }

    /** 检查非 admin 用户是否有权访问该预设 */
    private boolean checkPresetAccess(String presetId, String currentSystemId) {
        if (currentSystemId == null) return true; // admin, no filtering
        ColumnPreset preset = adminService.getPreset(presetId);
        return preset != null && currentSystemId.equals(preset.getSystemId());
    }

    // ==================== Dashboard ====================

    @GetMapping("/stats")
    public R<Map<String, Object>> getDashboardStats(HttpServletRequest request) {
        String role = getRole(request);
        if (!canRead(role)) return R.unauthorized("无权限");
        String systemId = isAdmin(role) ? null : getSystemId(request);
        return R.ok(adminService.getDashboardStats(systemId));
    }

    // ==================== System ====================

    @GetMapping("/systems")
    public R<List<CdsSystem>> listSystems(HttpServletRequest request) {
        String role = getRole(request);
        if (!canRead(role)) return R.unauthorized("无权限");
        if (isAdmin(role)) {
            return R.ok(adminService.listSystems());
        } else {
            return R.ok(adminService.listSystemsBySystemId(getSystemId(request)));
        }
    }

    @PostMapping("/systems")
    public R<CdsSystem> createSystem(@RequestBody SystemSaveRequest req, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return R.ok(adminService.createSystem(req));
    }

    @PutMapping("/systems/{id}")
    public R<CdsSystem> updateSystem(@PathVariable String id, @RequestBody SystemSaveRequest req, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        CdsSystem entity = adminService.updateSystem(id, req);
        return entity != null ? R.ok(entity) : R.notFound("系统不存在");
    }

    @DeleteMapping("/systems/{id}")
    public R<Void> deleteSystem(@PathVariable String id, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return adminService.deleteSystem(id) ? R.ok() : R.notFound("系统不存在");
    }

    // ==================== Business ====================

    @GetMapping("/businesses")
    public R<List<CdsBusiness>> listBusinesses(@RequestParam(required = false) String systemId, HttpServletRequest request) {
        String role = getRole(request);
        if (!canRead(role)) return R.unauthorized("无权限");
        // 非 admin 时强制使用用户的 systemId
        if (!isAdmin(role)) {
            systemId = getSystemId(request);
        }
        return R.ok(adminService.listBusinesses(systemId));
    }

    @PostMapping("/businesses")
    public R<CdsBusiness> createBusiness(@RequestBody BusinessSaveRequest req, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return R.ok(adminService.createBusiness(req));
    }

    @PutMapping("/businesses/{id}")
    public R<CdsBusiness> updateBusiness(@PathVariable String id, @RequestBody BusinessSaveRequest req, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        CdsBusiness entity = adminService.updateBusiness(id, req);
        return entity != null ? R.ok(entity) : R.notFound("业务不存在");
    }

    @DeleteMapping("/businesses/{id}")
    public R<Void> deleteBusiness(@PathVariable String id, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return adminService.deleteBusiness(id) ? R.ok() : R.notFound("业务不存在");
    }

    // ==================== Preset ====================

    @GetMapping("/presets")
    public R<List<PresetDetailResponse>> listPresets(
            @RequestParam(required = false) String systemId,
            @RequestParam(required = false) String businessId,
            HttpServletRequest request) {
        String role = getRole(request);
        if (!canRead(role)) return R.unauthorized("无权限");
        if (!isAdmin(role)) {
            systemId = getSystemId(request); // 强制过滤
        }
        return R.ok(adminService.listPresets(systemId, businessId));
    }

    @PostMapping("/presets")
    public R<ColumnPreset> createPreset(@RequestBody PresetSaveRequest req, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        // 非 admin 时强制使用自己的 systemId
        if (!isAdmin(role)) {
            req.setSystemId(getSystemId(request));
        }
        return R.ok(adminService.createPreset(req));
    }

    @PutMapping("/presets/{id}")
    public R<ColumnPreset> updatePreset(@PathVariable String id, @RequestBody PresetSaveRequest req, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role) && !checkPresetAccess(id, getSystemId(request))) return R.unauthorized("无权修改此预设");
        ColumnPreset entity = adminService.updatePreset(id, req);
        return entity != null ? R.ok(entity) : R.notFound("预设不存在");
    }

    @DeleteMapping("/presets/{id}")
    public R<Void> deletePreset(@PathVariable String id, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role) && !checkPresetAccess(id, getSystemId(request))) return R.unauthorized("无权删除此预设");
        return adminService.deletePreset(id) ? R.ok() : R.notFound("预设不存在");
    }

    // ==================== Group ====================

    @GetMapping("/presets/{presetId}/groups")
    public R<List<ColumnGroup>> listGroups(@PathVariable String presetId, HttpServletRequest request) {
        String role = getRole(request);
        if (!canRead(role)) return R.unauthorized("无权限");
        if (!isAdmin(role) && !checkPresetAccess(presetId, getSystemId(request))) return R.unauthorized("无权访问");
        return R.ok(adminService.listGroups(presetId));
    }

    @PostMapping("/presets/{presetId}/groups")
    public R<ColumnGroup> createGroup(@PathVariable String presetId, @RequestBody GroupSaveRequest req, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role) && !checkPresetAccess(presetId, getSystemId(request))) return R.unauthorized("无权操作");
        return R.ok(adminService.createGroup(presetId, req));
    }

    @PutMapping("/groups/{id}")
    public R<ColumnGroup> updateGroup(@PathVariable String id, @RequestBody GroupSaveRequest req, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role)) {
            ColumnGroup group = adminService.getGroupById(id);
            if (group == null || !checkPresetAccess(group.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        }
        ColumnGroup entity = adminService.updateGroup(id, req);
        return entity != null ? R.ok(entity) : R.notFound("分组不存在");
    }

    @DeleteMapping("/groups/{id}")
    public R<Void> deleteGroup(@PathVariable String id, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role)) {
            ColumnGroup group = adminService.getGroupById(id);
            if (group == null || !checkPresetAccess(group.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        }
        return adminService.deleteGroup(id) ? R.ok() : R.notFound("分组不存在");
    }

    // ==================== Column Definition ====================

    @GetMapping("/groups/{groupId}/columns")
    public R<List<ColumnDefinition>> listColumns(@PathVariable String groupId, HttpServletRequest request) {
        String role = getRole(request);
        if (!canRead(role)) return R.unauthorized("无权限");
        if (!isAdmin(role)) {
            ColumnGroup group = adminService.getGroupById(groupId);
            if (group == null || !checkPresetAccess(group.getPresetId(), getSystemId(request))) return R.unauthorized("无权访问");
        }
        return R.ok(adminService.listColumns(groupId));
    }

    @PostMapping("/groups/{groupId}/columns")
    public R<ColumnDefinition> createColumn(@PathVariable String groupId, @RequestBody ColumnDefSaveRequest req, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role)) {
            ColumnGroup group = adminService.getGroupById(groupId);
            if (group == null || !checkPresetAccess(group.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        }
        return R.ok(adminService.createColumn(groupId, req));
    }

    @PutMapping("/columns/{id}")
    public R<ColumnDefinition> updateColumn(@PathVariable String id, @RequestBody ColumnDefSaveRequest req, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role)) {
            ColumnDefinition col = adminService.getColumnById(id);
            if (col == null) return R.notFound("列定义不存在");
            ColumnGroup group = adminService.getGroupById(col.getGroupId());
            if (group == null || !checkPresetAccess(group.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        }
        ColumnDefinition entity = adminService.updateColumn(id, req);
        return entity != null ? R.ok(entity) : R.notFound("列定义不存在");
    }

    @DeleteMapping("/columns/{id}")
    public R<Void> deleteColumn(@PathVariable String id, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role)) {
            ColumnDefinition col = adminService.getColumnById(id);
            if (col == null) return R.notFound("列定义不存在");
            ColumnGroup group = adminService.getGroupById(col.getGroupId());
            if (group == null || !checkPresetAccess(group.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        }
        return adminService.deleteColumn(id) ? R.ok() : R.notFound("列定义不存在");
    }

    // ==================== User Override ====================

    @GetMapping("/overrides")
    public R<List<UserOverrideResponse>> listOverrides(
            @RequestParam(required = false) String presetId,
            HttpServletRequest request) {
        String role = getRole(request);
        if (!canRead(role)) return R.unauthorized("无权限");
        String filterSystemId = isAdmin(role) ? null : getSystemId(request);
        return R.ok(adminService.listOverrides(presetId, filterSystemId));
    }

    @PostMapping("/overrides")
    public R<Void> saveOverride(@RequestBody ColumnOverride ov, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role) && !checkPresetAccess(ov.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        return adminService.saveOverride(ov) ? R.ok() : R.fail("保存失败");
    }

    @DeleteMapping("/overrides/{id}")
    public R<Void> deleteOverride(@PathVariable Long id, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        // 非 admin 时检查覆盖记录的 preset 是否属于用户系统
        if (!isAdmin(role)) {
            ColumnOverride ov = adminService.getOverrideById(id);
            if (ov == null) return R.notFound("覆盖不存在");
            if (!checkPresetAccess(ov.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        }
        return adminService.deleteOverride(id) ? R.ok() : R.notFound("覆盖不存在");
    }

    @DeleteMapping("/overrides/batch")
    public R<Integer> deleteOverridesByPresetUser(
            @RequestParam String presetId,
            @RequestParam String userId,
            HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return R.ok(adminService.deleteOverridesByPresetUser(presetId, userId));
    }

    // ==================== Role Preset ====================

    @GetMapping("/role-presets")
    public R<List<RolePresetResponse>> listRolePresets(
            @RequestParam(required = false) String presetId,
            HttpServletRequest request) {
        String role = getRole(request);
        if (!canRead(role)) return R.unauthorized("无权限");
        String filterSystemId = isAdmin(role) ? null : getSystemId(request);
        return R.ok(adminService.listRolePresets(presetId, filterSystemId));
    }

    @PostMapping("/role-presets")
    public R<Void> saveRolePreset(@RequestBody CdsRolePreset rp, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role) && !checkPresetAccess(rp.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        return adminService.saveRolePreset(rp) ? R.ok() : R.fail("保存失败");
    }

    @DeleteMapping("/role-presets/{id}")
    public R<Void> deleteRolePreset(@PathVariable Long id, HttpServletRequest request) {
        String role = getRole(request);
        if (!isEditor(role)) return R.unauthorized("需要编辑权限");
        if (!isAdmin(role)) {
            CdsRolePreset rp = adminService.getRolePresetById(id);
            if (rp == null) return R.notFound("角色预设不存在");
            if (!checkPresetAccess(rp.getPresetId(), getSystemId(request))) return R.unauthorized("无权操作");
        }
        return adminService.deleteRolePreset(id) ? R.ok() : R.notFound("角色预设不存在");
    }

    // ==================== User Management ====================

    @GetMapping("/users")
    public R<List<SysUser>> listUsers(HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return R.ok(adminService.listUsers());
    }

    @PostMapping("/users")
    public R<SysUser> createUser(@RequestBody UserSaveRequest req, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        if (req.getUsername() == null || req.getUsername().isBlank()) return R.fail("用户名不能为空");
        if (req.getPassword() == null || req.getPassword().isBlank()) return R.fail("密码不能为空");
        return R.ok(adminService.createUser(req.getUsername(), req.getPassword(), req.getDisplayName(), req.getRole(), req.getSystemId()));
    }

    @DeleteMapping("/users/{id}")
    public R<Void> deleteUser(@PathVariable String id, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return adminService.deleteUser(id) ? R.ok() : R.notFound("用户不存在");
    }

    // ==================== Role Management ====================

    @GetMapping("/roles")
    public R<List<SysRole>> listRoles(HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return R.ok(adminService.listRoles());
    }

    @PostMapping("/roles")
    public R<SysRole> createRole(@RequestBody SysRole role, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        if (role.getName() == null || role.getName().isBlank()) return R.fail("角色名不能为空");
        return R.ok(adminService.createRole(role.getName(), role.getDescription()));
    }

    @DeleteMapping("/roles/{id}")
    public R<Void> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(getRole(request))) return R.unauthorized("需要管理员权限");
        return adminService.deleteRole(id) ? R.ok() : R.notFound("角色不存在");
    }
}

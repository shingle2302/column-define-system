package com.cds.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.cds.dto.*;
import com.cds.entity.*;
import com.cds.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CdsSystemMapper systemMapper;
    private final CdsBusinessMapper businessMapper;
    private final ColumnPresetMapper presetMapper;
    private final ColumnGroupMapper groupMapper;
    private final ColumnDefinitionMapper definitionMapper;
    private final ColumnOverrideMapper overrideMapper;
    private final CdsRolePresetMapper rolePresetMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    // ==================== System CRUD ====================

    public List<CdsSystem> listSystems() {
        return systemMapper.selectList(new LambdaQueryWrapper<CdsSystem>().orderByAsc(CdsSystem::getCreatedAt));
    }

    public List<CdsSystem> listSystemsBySystemId(String systemId) {
        return systemMapper.selectList(new LambdaQueryWrapper<CdsSystem>()
                .eq(CdsSystem::getId, systemId));
    }

    public CdsSystem getSystem(String id) {
        return systemMapper.selectById(id);
    }

    @Transactional
    public CdsSystem createSystem(SystemSaveRequest req) {
        CdsSystem entity = new CdsSystem();
        entity.setId(IdWorker.getIdStr());
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setDescription(req.getDescription());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : "active");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        systemMapper.insert(entity);
        return entity;
    }

    @Transactional
    public CdsSystem updateSystem(String id, SystemSaveRequest req) {
        CdsSystem entity = systemMapper.selectById(id);
        if (entity == null) return null;
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getCode() != null) entity.setCode(req.getCode());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getStatus() != null) entity.setStatus(req.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        systemMapper.updateById(entity);
        return entity;
    }

    @Transactional
    public boolean deleteSystem(String id) {
        return systemMapper.deleteById(id) > 0;
    }

    // ==================== Business CRUD ====================

    public List<CdsBusiness> listBusinesses(String systemId) {
        LambdaQueryWrapper<CdsBusiness> qw = new LambdaQueryWrapper<CdsBusiness>()
                .orderByAsc(CdsBusiness::getSortOrder);
        if (systemId != null) {
            qw.eq(CdsBusiness::getSystemId, systemId);
        }
        List<CdsBusiness> list = businessMapper.selectList(qw);
        // fill system name
        for (CdsBusiness b : list) {
            CdsSystem sys = systemMapper.selectById(b.getSystemId());
            if (sys != null) b.setSystemName(sys.getName());
        }
        return list;
    }

    @Transactional
    public CdsBusiness createBusiness(BusinessSaveRequest req) {
        CdsBusiness entity = new CdsBusiness();
        entity.setId(IdWorker.getIdStr());
        entity.setSystemId(req.getSystemId());
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setDescription(req.getDescription());
        entity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        businessMapper.insert(entity);
        // fill system name
        CdsSystem sys = systemMapper.selectById(entity.getSystemId());
        if (sys != null) entity.setSystemName(sys.getName());
        return entity;
    }

    @Transactional
    public CdsBusiness updateBusiness(String id, BusinessSaveRequest req) {
        CdsBusiness entity = businessMapper.selectById(id);
        if (entity == null) return null;
        if (req.getSystemId() != null) entity.setSystemId(req.getSystemId());
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getCode() != null) entity.setCode(req.getCode());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getSortOrder() != null) entity.setSortOrder(req.getSortOrder());
        entity.setUpdatedAt(LocalDateTime.now());
        businessMapper.updateById(entity);
        // fill system name
        CdsSystem sys = systemMapper.selectById(entity.getSystemId());
        if (sys != null) entity.setSystemName(sys.getName());
        return entity;
    }

    @Transactional
    public boolean deleteBusiness(String id) {
        return businessMapper.deleteById(id) > 0;
    }

    // ==================== Preset CRUD ====================

    public List<PresetDetailResponse> listPresets(String systemId, String businessId) {
        LambdaQueryWrapper<ColumnPreset> qw = new LambdaQueryWrapper<ColumnPreset>()
                .orderByAsc(ColumnPreset::getCreatedAt);
        if (systemId != null) qw.eq(ColumnPreset::getSystemId, systemId);
        if (businessId != null) qw.eq(ColumnPreset::getBusinessId, businessId);
        List<ColumnPreset> presets = presetMapper.selectList(qw);
        List<PresetDetailResponse> result = new ArrayList<>();
        for (ColumnPreset p : presets) {
            String sysName = "";
            String bizName = "";
            if (p.getSystemId() != null) {
                CdsSystem sys = systemMapper.selectById(p.getSystemId());
                if (sys != null) sysName = sys.getName();
            }
            if (p.getBusinessId() != null) {
                CdsBusiness biz = businessMapper.selectById(p.getBusinessId());
                if (biz != null) bizName = biz.getName();
            }
            result.add(new PresetDetailResponse(
                    p.getId(), p.getName(), p.getDescription(),
                    p.getSystemId(), sysName, p.getBusinessId(), bizName,
                    p.getAllowCrossGroup(), p.getIsDefault(),
                    p.getMinVisibleColumns(), p.getMaxVisibleColumns(),
                    p.getVersion(), p.getCreatedAt(), p.getUpdatedAt()
            ));
        }
        return result;
    }

    @Transactional
    public ColumnPreset createPreset(PresetSaveRequest req) {
        ColumnPreset entity = new ColumnPreset();
        entity.setId(IdWorker.getIdStr());
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setSystemId(req.getSystemId());
        entity.setBusinessId(req.getBusinessId());
        entity.setAllowCrossGroup(req.getAllowCrossGroup() != null ? req.getAllowCrossGroup() : false);
        entity.setIsDefault(req.getIsDefault() != null ? req.getIsDefault() : false);
        entity.setMinVisibleColumns(req.getMinVisibleColumns());
        entity.setMaxVisibleColumns(req.getMaxVisibleColumns());
        entity.setVersion(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        presetMapper.insert(entity);
        return entity;
    }

    @Transactional
    public ColumnPreset updatePreset(String id, PresetSaveRequest req) {
        ColumnPreset entity = presetMapper.selectById(id);
        if (entity == null) return null;
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getSystemId() != null) entity.setSystemId(req.getSystemId());
        if (req.getBusinessId() != null) entity.setBusinessId(req.getBusinessId());
        if (req.getAllowCrossGroup() != null) entity.setAllowCrossGroup(req.getAllowCrossGroup());
        if (req.getIsDefault() != null) entity.setIsDefault(req.getIsDefault());
        if (req.getMinVisibleColumns() != null) entity.setMinVisibleColumns(req.getMinVisibleColumns());
        if (req.getMaxVisibleColumns() != null) entity.setMaxVisibleColumns(req.getMaxVisibleColumns());
        entity.setUpdatedAt(LocalDateTime.now());
        presetMapper.updateById(entity);
        return entity;
    }

    @Transactional
    public boolean deletePreset(String id) {
        return presetMapper.deleteById(id) > 0;
    }

    public ColumnPreset getPreset(String id) {
        return presetMapper.selectById(id);
    }

    public ColumnGroup getGroupById(String id) {
        if (id == null) return null;
        return groupMapper.selectById(id);
    }

    public ColumnDefinition getColumnById(String id) {
        if (id == null) return null;
        return definitionMapper.selectById(id);
    }

    public ColumnOverride getOverrideById(Long id) {
        if (id == null) return null;
        return overrideMapper.selectById(id);
    }

    public CdsRolePreset getRolePresetById(Long id) {
        if (id == null) return null;
        return rolePresetMapper.selectById(id);
    }

    /** 仪表盘统计（可按系统过滤） */
    public Map<String, Object> getDashboardStats(String systemId) {
        Map<String, Object> stats = new java.util.LinkedHashMap<>();

        // 系统数
        if (systemId != null) {
            stats.put("systems", systemMapper.selectCount(new LambdaQueryWrapper<CdsSystem>().eq(CdsSystem::getId, systemId)));
        } else {
            stats.put("systems", systemMapper.selectCount(null));
        }

        // 业务模块数
        if (systemId != null) {
            stats.put("businesses", businessMapper.selectCount(new LambdaQueryWrapper<CdsBusiness>().eq(CdsBusiness::getSystemId, systemId)));
        } else {
            stats.put("businesses", businessMapper.selectCount(null));
        }

        // 预设数
        if (systemId != null) {
            stats.put("presets", presetMapper.selectCount(new LambdaQueryWrapper<ColumnPreset>().eq(ColumnPreset::getSystemId, systemId)));
        } else {
            stats.put("presets", presetMapper.selectCount(null));
        }

        // 列定义数
        long columnCount;
        if (systemId != null) {
            List<String> presetIds = presetMapper.selectList(new LambdaQueryWrapper<ColumnPreset>()
                            .eq(ColumnPreset::getSystemId, systemId).select(ColumnPreset::getId))
                    .stream().map(ColumnPreset::getId).toList();
            if (presetIds.isEmpty()) {
                columnCount = 0;
            } else {
                List<String> groupIds = groupMapper.selectList(new LambdaQueryWrapper<ColumnGroup>()
                                .in(ColumnGroup::getPresetId, presetIds).select(ColumnGroup::getId))
                        .stream().map(ColumnGroup::getId).toList();
                columnCount = groupIds.isEmpty() ? 0 :
                        definitionMapper.selectCount(new LambdaQueryWrapper<ColumnDefinition>().in(ColumnDefinition::getGroupId, groupIds));
            }
        } else {
            columnCount = definitionMapper.selectCount(null);
        }
        stats.put("columns", columnCount);

        return stats;
    }

    // ==================== Group CRUD ====================

    public List<ColumnGroup> listGroups(String presetId) {
        return groupMapper.selectList(new LambdaQueryWrapper<ColumnGroup>()
                .eq(ColumnGroup::getPresetId, presetId)
                .orderByAsc(ColumnGroup::getSortOrder));
    }

    @Transactional
    public ColumnGroup createGroup(String presetId, GroupSaveRequest req) {
        ColumnGroup entity = new ColumnGroup();
        entity.setId(IdWorker.getIdStr());
        entity.setLabel(req.getLabel());
        entity.setParentId(req.getParentId());
        entity.setPresetId(presetId);
        entity.setCollapsed(req.getCollapsed() != null ? req.getCollapsed() : false);
        entity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        groupMapper.insert(entity);
        return entity;
    }

    @Transactional
    public ColumnGroup updateGroup(String id, GroupSaveRequest req) {
        ColumnGroup entity = groupMapper.selectById(id);
        if (entity == null) return null;
        if (req.getLabel() != null) entity.setLabel(req.getLabel());
        if (req.getParentId() != null) entity.setParentId(req.getParentId());
        if (req.getSortOrder() != null) entity.setSortOrder(req.getSortOrder());
        if (req.getCollapsed() != null) entity.setCollapsed(req.getCollapsed());
        entity.setUpdatedAt(LocalDateTime.now());
        groupMapper.updateById(entity);
        return entity;
    }

    @Transactional
    public boolean deleteGroup(String id) {
        return groupMapper.deleteById(id) > 0;
    }

    // ==================== Column Definition CRUD ====================

    public List<ColumnDefinition> listColumns(String groupId) {
        return definitionMapper.selectList(new LambdaQueryWrapper<ColumnDefinition>()
                .eq(ColumnDefinition::getGroupId, groupId)
                .orderByAsc(ColumnDefinition::getSortOrder));
    }

    @Transactional
    public ColumnDefinition createColumn(String groupId, ColumnDefSaveRequest req) {
        ColumnDefinition entity = new ColumnDefinition();
        entity.setId(IdWorker.getIdStr());
        entity.setLabel(req.getLabel());
        entity.setField(req.getField());
        entity.setType(req.getType());
        entity.setWidth(req.getWidth() != null ? req.getWidth() : 150);
        entity.setMinWidth(req.getMinWidth());
        entity.setMaxWidth(req.getMaxWidth());
        entity.setSortable(req.getSortable() != null ? req.getSortable() : false);
        entity.setFilterable(req.getFilterable() != null ? req.getFilterable() : false);
        entity.setResizable(req.getResizable() != null ? req.getResizable() : true);
        entity.setVisible(req.getVisible() != null ? req.getVisible() : true);
        entity.setPinned(req.getPinned());
        entity.setLocked(req.getLocked() != null ? req.getLocked() : false);
        entity.setAllowHide(req.getAllowHide() != null ? req.getAllowHide() : true);
        entity.setAllowSort(req.getAllowSort() != null ? req.getAllowSort() : true);
        entity.setAllowResize(req.getAllowResize() != null ? req.getAllowResize() : true);
        entity.setAllowPin(req.getAllowPin() != null ? req.getAllowPin() : true);
        entity.setAlign(req.getAlign() != null ? req.getAlign() : "left");
        entity.setValidationJson(req.getValidationJson());
        entity.setOptionsJson(req.getOptionsJson());
        entity.setDefaultValue(req.getDefaultValue());
        entity.setMetaJson(req.getMetaJson());
        entity.setGroupId(groupId);
        entity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        definitionMapper.insert(entity);
        return entity;
    }

    @Transactional
    public ColumnDefinition updateColumn(String id, ColumnDefSaveRequest req) {
        ColumnDefinition entity = definitionMapper.selectById(id);
        if (entity == null) return null;
        if (req.getLabel() != null) entity.setLabel(req.getLabel());
        if (req.getField() != null) entity.setField(req.getField());
        if (req.getType() != null) entity.setType(req.getType());
        if (req.getWidth() != null) entity.setWidth(req.getWidth());
        if (req.getMinWidth() != null) entity.setMinWidth(req.getMinWidth());
        if (req.getMaxWidth() != null) entity.setMaxWidth(req.getMaxWidth());
        if (req.getSortable() != null) entity.setSortable(req.getSortable());
        if (req.getFilterable() != null) entity.setFilterable(req.getFilterable());
        if (req.getResizable() != null) entity.setResizable(req.getResizable());
        if (req.getVisible() != null) entity.setVisible(req.getVisible());
        if (req.getPinned() != null) entity.setPinned(req.getPinned());
        if (req.getLocked() != null) entity.setLocked(req.getLocked());
        if (req.getAllowHide() != null) entity.setAllowHide(req.getAllowHide());
        if (req.getAllowSort() != null) entity.setAllowSort(req.getAllowSort());
        if (req.getAllowResize() != null) entity.setAllowResize(req.getAllowResize());
        if (req.getAllowPin() != null) entity.setAllowPin(req.getAllowPin());
        if (req.getAlign() != null) entity.setAlign(req.getAlign());
        if (req.getValidationJson() != null) entity.setValidationJson(req.getValidationJson());
        if (req.getOptionsJson() != null) entity.setOptionsJson(req.getOptionsJson());
        if (req.getDefaultValue() != null) entity.setDefaultValue(req.getDefaultValue());
        if (req.getMetaJson() != null) entity.setMetaJson(req.getMetaJson());
        if (req.getSortOrder() != null) entity.setSortOrder(req.getSortOrder());
        entity.setUpdatedAt(LocalDateTime.now());
        definitionMapper.updateById(entity);
        return entity;
    }

    @Transactional
    public boolean deleteColumn(String id) {
        return definitionMapper.deleteById(id) > 0;
    }

    // ==================== User Override Management ====================

    /** 列出所有用户覆盖（可按预设筛选，可按系统过滤） */
    public List<UserOverrideResponse> listOverrides(String presetId, String filterSystemId) {
        // 解析允许的预设 ID 集合
        java.util.Set<String> allowedPresetIds = null;
        if (filterSystemId != null && !filterSystemId.isEmpty()) {
            allowedPresetIds = presetMapper.selectList(
                    new LambdaQueryWrapper<ColumnPreset>()
                            .eq(ColumnPreset::getSystemId, filterSystemId)
                            .select(ColumnPreset::getId)
            ).stream().map(ColumnPreset::getId).collect(Collectors.toSet());
            if (allowedPresetIds.isEmpty()) return List.of();
            if (presetId != null && !presetId.isEmpty() && !allowedPresetIds.contains(presetId)) return List.of();
        }

        LambdaQueryWrapper<ColumnOverride> qw = new LambdaQueryWrapper<ColumnOverride>()
                .orderByDesc(ColumnOverride::getUpdatedAt);
        if (allowedPresetIds != null) {
            if (presetId != null && !presetId.isEmpty()) {
                qw.eq(ColumnOverride::getPresetId, presetId);
            } else {
                qw.in(ColumnOverride::getPresetId, allowedPresetIds);
            }
        } else if (presetId != null && !presetId.isEmpty()) {
            qw.eq(ColumnOverride::getPresetId, presetId);
        }
        List<ColumnOverride> overrides = overrideMapper.selectList(qw);

        // 构建名称缓存
        Map<String, String> presetNames = new HashMap<>();
        Map<String, String> userDisplayNames = new HashMap<>();
        Map<String, String[]> columnInfos = new HashMap<>(); // columnId -> [label, field]

        return overrides.stream().map(ov -> {
            // 预设名称
            String pName = presetNames.computeIfAbsent(ov.getPresetId(), pid -> {
                ColumnPreset p = presetMapper.selectById(pid);
                return p != null ? p.getName() : pid;
            });
            // 用户名称
            String uName = userDisplayNames.computeIfAbsent(ov.getUserId(), uid -> {
                SysUser u = userMapper.selectById(uid);
                return u != null ? u.getDisplayName() : uid;
            });
            // 列信息
            String[] colInfo = columnInfos.computeIfAbsent(ov.getColumnId(), cid -> {
                ColumnDefinition cd = definitionMapper.selectById(cid);
                return cd != null ? new String[]{cd.getLabel(), cd.getField()} : new String[]{cid, cid};
            });

            return new UserOverrideResponse(
                    ov.getId(), ov.getPresetId(), pName,
                    ov.getUserId(), uName, ov.getColumnId(),
                    colInfo[0], colInfo[1],
                    ov.getOverrideOrder(), ov.getOverrideWidth(),
                    ov.getOverrideVisible(), ov.getOverridePinned(),
                    ov.getVersion(), ov.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteOverride(Long id) {
        return overrideMapper.deleteById(id) > 0;
    }

    @Transactional
    public boolean saveOverride(ColumnOverride ov) {
        LambdaQueryWrapper<ColumnOverride> qw = new LambdaQueryWrapper<ColumnOverride>()
                .eq(ColumnOverride::getPresetId, ov.getPresetId())
                .eq(ColumnOverride::getUserId, ov.getUserId())
                .eq(ColumnOverride::getColumnId, ov.getColumnId());
        ColumnOverride existing = overrideMapper.selectOne(qw);
        ov.setUpdatedAt(LocalDateTime.now());
        if (existing != null) {
            ov.setId(existing.getId());
            return overrideMapper.updateById(ov) > 0;
        } else {
            return overrideMapper.insert(ov) > 0;
        }
    }

    @Transactional
    public int deleteOverridesByPresetUser(String presetId, String userId) {
        return overrideMapper.delete(new LambdaQueryWrapper<ColumnOverride>()
                .eq(ColumnOverride::getPresetId, presetId)
                .eq(ColumnOverride::getUserId, userId));
    }

    // ==================== Role Preset Management ====================

    /** 列出所有角色预设覆盖（可按预设筛选，可按系统过滤） */
    public List<RolePresetResponse> listRolePresets(String presetId, String filterSystemId) {
        // 解析允许的预设 ID 集合
        java.util.Set<String> allowedPresetIds = null;
        if (filterSystemId != null && !filterSystemId.isEmpty()) {
            allowedPresetIds = presetMapper.selectList(
                    new LambdaQueryWrapper<ColumnPreset>()
                            .eq(ColumnPreset::getSystemId, filterSystemId)
                            .select(ColumnPreset::getId)
            ).stream().map(ColumnPreset::getId).collect(Collectors.toSet());
            if (allowedPresetIds.isEmpty()) return List.of();
            if (presetId != null && !presetId.isEmpty() && !allowedPresetIds.contains(presetId)) return List.of();
        }

        LambdaQueryWrapper<CdsRolePreset> qw = new LambdaQueryWrapper<CdsRolePreset>()
                .orderByDesc(CdsRolePreset::getUpdatedAt);
        if (allowedPresetIds != null) {
            if (presetId != null && !presetId.isEmpty()) {
                qw.eq(CdsRolePreset::getPresetId, presetId);
            } else {
                qw.in(CdsRolePreset::getPresetId, allowedPresetIds);
            }
        } else if (presetId != null && !presetId.isEmpty()) {
            qw.eq(CdsRolePreset::getPresetId, presetId);
        }
        List<CdsRolePreset> list = rolePresetMapper.selectList(qw);

        Map<String, String> presetNames = new HashMap<>();
        Map<String, String[]> columnInfos = new HashMap<>();

        return list.stream().map(rp -> {
            String pName = presetNames.computeIfAbsent(rp.getPresetId(), pid -> {
                ColumnPreset p = presetMapper.selectById(pid);
                return p != null ? p.getName() : pid;
            });
            String[] colInfo = columnInfos.computeIfAbsent(rp.getColumnId(), cid -> {
                ColumnDefinition cd = definitionMapper.selectById(cid);
                return cd != null ? new String[]{cd.getLabel(), cd.getField()} : new String[]{cid, cid};
            });

            return new RolePresetResponse(
                    rp.getId(), rp.getPresetId(), pName,
                    rp.getRole(), rp.getColumnId(),
                    colInfo[0], colInfo[1],
                    rp.getOverrideOrder(), rp.getOverrideWidth(),
                    rp.getOverrideVisible(), rp.getOverridePinned(),
                    rp.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteRolePreset(Long id) {
        return rolePresetMapper.deleteById(id) > 0;
    }

    @Transactional
    public boolean saveRolePreset(CdsRolePreset rp) {
        // 存在则更新，不存在则插入
        LambdaQueryWrapper<CdsRolePreset> qw = new LambdaQueryWrapper<CdsRolePreset>()
                .eq(CdsRolePreset::getPresetId, rp.getPresetId())
                .eq(CdsRolePreset::getRole, rp.getRole())
                .eq(CdsRolePreset::getColumnId, rp.getColumnId());
        CdsRolePreset existing = rolePresetMapper.selectOne(qw);
        rp.setUpdatedAt(LocalDateTime.now());
        if (existing != null) {
            rp.setId(existing.getId());
            return rolePresetMapper.updateById(rp) > 0;
        } else {
            return rolePresetMapper.insert(rp) > 0;
        }
    }

    // ==================== User Management ====================

    public List<SysUser> listUsers() {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .orderByAsc(SysUser::getCreatedAt));
    }

    @Transactional
    public SysUser createUser(String username, String password, String displayName, String role, String systemId) {
        SysUser user = new SysUser();
        user.setId(IdWorker.getIdStr());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setDisplayName(displayName != null ? displayName : username);
        user.setRole(role != null ? role : "user");
        user.setSystemId(systemId);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public boolean deleteUser(String id) {
        return userMapper.deleteById(id) > 0;
    }

    // ==================== Role Management ====================

    public List<SysRole> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getCreatedAt));
    }

    @Transactional
    public SysRole createRole(String name, String description) {
        SysRole role = new SysRole();
        role.setName(name);
        role.setDescription(description);
        role.setCreatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    @Transactional
    public boolean deleteRole(Long id) {
        return roleMapper.deleteById(id) > 0;
    }
}

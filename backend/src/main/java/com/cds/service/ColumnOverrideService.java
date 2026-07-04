package com.cds.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cds.dto.OverrideRequest;
import com.cds.dto.OverrideResponse;
import com.cds.dto.OverrideResponse.ColumnOverrideData;
import com.cds.entity.CdsRolePreset;
import com.cds.entity.ColumnDefinition;
import com.cds.entity.ColumnGroup;
import com.cds.entity.ColumnOverride;
import com.cds.entity.ColumnPreset;
import com.cds.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ColumnOverrideService {

    private final ColumnOverrideMapper overrideMapper;
    private final CdsRolePresetMapper rolePresetMapper;
    private final ColumnPresetMapper presetMapper;
    private final ColumnGroupMapper groupMapper;
    private final ColumnDefinitionMapper definitionMapper;

    /**
     * 多级覆盖合并：用户覆盖 > 角色覆盖 > 预设默认值
     */
    public OverrideResponse load(String presetId, String userId, String role) {
        ColumnPreset preset = presetMapper.selectById(presetId);
        if (preset == null) return null;

        // 1. 获取预设中所有列的定义（作为基准）
        Map<String, ColumnOverrideData> baseMap = buildBaseFromDefinitions(presetId);

        // 2. 角色级覆盖
        if (role != null) {
            List<CdsRolePreset> roleOverrides = rolePresetMapper.selectList(
                    new LambdaQueryWrapper<CdsRolePreset>()
                            .eq(CdsRolePreset::getPresetId, presetId)
                            .eq(CdsRolePreset::getRole, role));
            for (CdsRolePreset rp : roleOverrides) {
                baseMap.computeIfPresent(rp.getColumnId(), (k, v) -> applyRoleOverride(v, rp));
            }
        }

        // 3. 用户级覆盖（优先级最高）
        List<ColumnOverride> userOverrides = overrideMapper.selectList(
                new LambdaQueryWrapper<ColumnOverride>()
                        .eq(ColumnOverride::getPresetId, presetId)
                        .eq(ColumnOverride::getUserId, userId));

        for (ColumnOverride ov : userOverrides) {
            baseMap.computeIfPresent(ov.getColumnId(), (k, v) -> applyUserOverride(v, ov));
        }

        int version = userOverrides.stream()
                .mapToInt(ColumnOverride::getVersion)
                .filter(v -> v > 0)
                .max().orElse(preset.getVersion() != null ? preset.getVersion() : 1);
        LocalDateTime maxUpdate = userOverrides.stream()
                .map(ColumnOverride::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(preset.getUpdatedAt());

        return OverrideResponse.builder()
                .presetId(presetId)
                .columnOverrides(baseMap)
                .version(version)
                .updatedAt(toMillis(maxUpdate))
                .build();
    }

    @Transactional
    public void save(String presetId, String userId, OverrideRequest request) {
        overrideMapper.delete(new LambdaQueryWrapper<ColumnOverride>()
                .eq(ColumnOverride::getPresetId, presetId)
                .eq(ColumnOverride::getUserId, userId));

        if (request.getColumnOverrides() == null || request.getColumnOverrides().isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (var entry : request.getColumnOverrides().entrySet()) {
            ColumnOverride override = new ColumnOverride();
            override.setPresetId(presetId);
            override.setUserId(userId);
            override.setColumnId(entry.getKey());
            var item = entry.getValue();
            override.setOverrideOrder(item.getOrder());
            override.setOverrideWidth(item.getWidth());
            override.setOverrideVisible(item.getVisible());
            override.setOverridePinned(item.getPinned());
            override.setVersion(request.getVersion() != null ? request.getVersion() : 1);
            override.setUpdatedAt(now);
            overrideMapper.insert(override);
        }
    }

    private Map<String, ColumnOverrideData> buildBaseFromDefinitions(String presetId) {
        Map<String, ColumnOverrideData> map = new LinkedHashMap<>();
        List<ColumnGroup> groups = groupMapper.selectList(
                new LambdaQueryWrapper<ColumnGroup>()
                        .eq(ColumnGroup::getPresetId, presetId)
                        .orderByAsc(ColumnGroup::getSortOrder));
        for (ColumnGroup g : groups) {
            List<ColumnDefinition> defs = definitionMapper.selectList(
                    new LambdaQueryWrapper<ColumnDefinition>()
                            .eq(ColumnDefinition::getGroupId, g.getId())
                            .orderByAsc(ColumnDefinition::getSortOrder));
            for (ColumnDefinition d : defs) {
                map.put(d.getId(), ColumnOverrideData.builder()
                        .order(d.getSortOrder())
                        .width(d.getWidth())
                        .visible(d.getVisible() != null ? d.getVisible() : true)
                        .pinned(d.getPinned())
                        .build());
            }
        }
        return map;
    }

    private ColumnOverrideData applyRoleOverride(ColumnOverrideData base, CdsRolePreset rp) {
        return ColumnOverrideData.builder()
                .order(rp.getOverrideOrder() != null ? rp.getOverrideOrder() : base.getOrder())
                .width(rp.getOverrideWidth() != null ? rp.getOverrideWidth() : base.getWidth())
                .visible(rp.getOverrideVisible() != null ? rp.getOverrideVisible() : base.getVisible())
                .pinned(rp.getOverridePinned() != null ? rp.getOverridePinned() : base.getPinned())
                .build();
    }

    private ColumnOverrideData applyUserOverride(ColumnOverrideData base, ColumnOverride ov) {
        return ColumnOverrideData.builder()
                .order(ov.getOverrideOrder() != null ? ov.getOverrideOrder() : base.getOrder())
                .width(ov.getOverrideWidth() != null ? ov.getOverrideWidth() : base.getWidth())
                .visible(ov.getOverrideVisible() != null ? ov.getOverrideVisible() : base.getVisible())
                .pinned(ov.getOverridePinned() != null ? ov.getOverridePinned() : base.getPinned())
                .build();
    }

    private Long toMillis(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}

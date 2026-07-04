package com.cds.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cds.dto.PresetResponse;
import com.cds.entity.CdsSystem;
import com.cds.entity.ColumnDefinition;
import com.cds.entity.ColumnGroup;
import com.cds.entity.ColumnPreset;
import com.cds.mapper.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColumnPresetService {

    private final ColumnPresetMapper presetMapper;
    private final ColumnGroupMapper groupMapper;
    private final ColumnDefinitionMapper definitionMapper;
    private final CdsSystemMapper systemMapper;
    private final ObjectMapper objectMapper;

    public List<PresetResponse> listAll() {
        List<ColumnPreset> presets = presetMapper.selectList(null);
        return presets.stream()
                .sorted(Comparator.comparing(ColumnPreset::getIsDefault, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    /** 按系统 code 查询预设 */
    public List<PresetResponse> listBySystem(String systemCode) {
        CdsSystem sys = systemMapper.selectOne(
                new LambdaQueryWrapper<CdsSystem>().eq(CdsSystem::getCode, systemCode));
        if (sys == null) return Collections.emptyList();
        List<ColumnPreset> presets = presetMapper.selectList(
                new LambdaQueryWrapper<ColumnPreset>().eq(ColumnPreset::getSystemId, sys.getId()));
        return presets.stream()
                .sorted(Comparator.comparing(ColumnPreset::getIsDefault, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    /** 按业务模块 ID 查询预设 */
    public List<PresetResponse> listByBusiness(String businessId) {
        List<ColumnPreset> presets = presetMapper.selectList(
                new LambdaQueryWrapper<ColumnPreset>().eq(ColumnPreset::getBusinessId, businessId));
        return presets.stream()
                .sorted(Comparator.comparing(ColumnPreset::getIsDefault, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    public PresetResponse getById(String id) {
        ColumnPreset preset = presetMapper.selectById(id);
        if (preset == null) {
            throw new RuntimeException("预设方案不存在: " + id);
        }
        return buildResponse(preset);
    }

    private PresetResponse buildResponse(ColumnPreset preset) {
        List<ColumnGroup> groups = groupMapper.selectList(
                new LambdaQueryWrapper<ColumnGroup>()
                        .eq(ColumnGroup::getPresetId, preset.getId())
                        .orderByAsc(ColumnGroup::getSortOrder));

        List<PresetResponse.GroupNode> groupNodes = groups.stream().map(g -> {
            List<ColumnDefinition> defs = definitionMapper.selectList(
                    new LambdaQueryWrapper<ColumnDefinition>()
                            .eq(ColumnDefinition::getGroupId, g.getId())
                            .orderByAsc(ColumnDefinition::getSortOrder));

            List<PresetResponse.ColumnNode> children = defs.stream().map(d ->
                PresetResponse.ColumnNode.builder()
                    .id(d.getId())
                    .label(d.getLabel())
                    .field(d.getField())
                    .type(d.getType())
                    .width(d.getWidth())
                    .minWidth(d.getMinWidth())
                    .maxWidth(d.getMaxWidth())
                    .sortable(d.getSortable() != null ? d.getSortable() : false)
                    .filterable(d.getFilterable() != null ? d.getFilterable() : false)
                    .resizable(d.getResizable() != null ? d.getResizable() : true)
                    .visible(d.getVisible() != null ? d.getVisible() : true)
                    .pinned(d.getPinned())
                    .locked(d.getLocked() != null ? d.getLocked() : false)
                    .allowHide(d.getAllowHide() != null ? d.getAllowHide() : true)
                    .allowSort(d.getAllowSort() != null ? d.getAllowSort() : true)
                    .allowResize(d.getAllowResize() != null ? d.getAllowResize() : true)
                    .allowPin(d.getAllowPin() != null ? d.getAllowPin() : true)
                    .align(d.getAlign())
                    .defaultValue(d.getDefaultValue())
                    .meta(d.getMetaJson() != null ? parseJson(d.getMetaJson(), new TypeReference<Map<String, Object>>() {}) : null)
                    .validation(d.getValidationJson() != null ? parseJson(d.getValidationJson(), new TypeReference<List<?>>() {}) : null)
                    .options(d.getOptionsJson() != null ? parseJson(d.getOptionsJson(), new TypeReference<List<PresetResponse.OptionNode>>() {}) : null)
                    .build()
            ).collect(Collectors.toList());

            return PresetResponse.GroupNode.builder()
                    .id(g.getId())
                    .label(g.getLabel())
                    .children(children)
                    .collapsed(g.getCollapsed())
                    .build();
        }).collect(Collectors.toList());

        return PresetResponse.builder()
                .id(preset.getId())
                .name(preset.getName())
                .description(preset.getDescription())
                .systemId(preset.getSystemId())
                .businessId(preset.getBusinessId())
                .groups(groupNodes)
                .allowCrossGroup(preset.getAllowCrossGroup())
                .version(preset.getVersion())
                .minVisibleColumns(preset.getMinVisibleColumns())
                .maxVisibleColumns(preset.getMaxVisibleColumns())
                .createdAt(preset.getCreatedAt() != null ? toMillis(preset.getCreatedAt()) : null)
                .updatedAt(preset.getUpdatedAt() != null ? toMillis(preset.getUpdatedAt()) : null)
                .build();
    }

    private <T> T parseJson(String json, TypeReference<T> ref) {
        try {
            return objectMapper.readValue(json, ref);
        } catch (Exception e) {
            return null;
        }
    }

    private Long toMillis(java.time.LocalDateTime dt) {
        return dt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}

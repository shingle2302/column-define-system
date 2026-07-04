package com.cds.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cds.dto.OverrideResponse;
import com.cds.dto.PresetResponse;
import com.cds.dto.SharedResponse;
import com.cds.entity.ColumnShare;
import com.cds.entity.ColumnOverride;
import com.cds.mapper.ColumnShareMapper;
import com.cds.mapper.ColumnOverrideMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ColumnShareService {

    private final ColumnShareMapper shareMapper;
    private final ColumnOverrideMapper overrideMapper;
    private final ColumnPresetService presetService;
    private final ObjectMapper objectMapper;

    @Transactional
    public String createShare(String presetId, String userId) {
        // Get current override data
        List<ColumnOverride> overrides = overrideMapper.selectList(
                new LambdaQueryWrapper<ColumnOverride>()
                        .eq(ColumnOverride::getPresetId, presetId)
                        .eq(ColumnOverride::getUserId, userId));

        ColumnShare share = new ColumnShare();
        share.setToken(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        share.setPresetId(presetId);
        share.setUserId(userId);

        try {
            share.setOverrideData(objectMapper.writeValueAsString(buildOverrideData(overrides)));
        } catch (Exception e) {
            throw new RuntimeException("序列化覆盖数据失败", e);
        }

        share.setCreatedAt(LocalDateTime.now());
        share.setExpiresAt(LocalDateTime.now().plusHours(24));
        shareMapper.insert(share);

        return share.getToken();
    }

    public SharedResponse loadShared(String token) {
        ColumnShare share = shareMapper.selectById(token);
        if (share == null) {
            throw new RuntimeException("分享不存在或已过期");
        }
        if (share.getExpiresAt() != null && share.getExpiresAt().isBefore(LocalDateTime.now())) {
            shareMapper.deleteById(token);
            throw new RuntimeException("分享已过期");
        }

        PresetResponse preset = presetService.getById(share.getPresetId());

        OverrideResponse override = null;
        try {
            Map<String, OverrideResponse.ColumnOverrideData> data = objectMapper.readValue(
                    share.getOverrideData(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, OverrideResponse.ColumnOverrideData.class));
            override = OverrideResponse.builder()
                    .presetId(share.getPresetId())
                    .columnOverrides(data)
                    .version(1)
                    .updatedAt(toMillis(share.getCreatedAt()))
                    .build();
        } catch (Exception e) {
            // ignore parse error, return without override
        }

        return SharedResponse.builder()
                .preset(preset)
                .override(override)
                .build();
    }

    private Map<String, Map<String, Object>> buildOverrideData(List<ColumnOverride> overrides) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (ColumnOverride ov : overrides) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("order", ov.getOverrideOrder());
            item.put("width", ov.getOverrideWidth());
            item.put("visible", ov.getOverrideVisible());
            item.put("pinned", ov.getOverridePinned());
            result.put(ov.getColumnId(), item);
        }
        return result;
    }

    private Long toMillis(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}

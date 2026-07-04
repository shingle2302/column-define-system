package com.cds.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cds.dto.SystemResponse;
import com.cds.dto.SystemResponse.BusinessNode;
import com.cds.entity.CdsBusiness;
import com.cds.entity.CdsSystem;
import com.cds.mapper.CdsBusinessMapper;
import com.cds.mapper.CdsSystemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final CdsSystemMapper systemMapper;
    private final CdsBusinessMapper businessMapper;

    public List<SystemResponse> listAll() {
        List<CdsSystem> systems = systemMapper.selectList(null);
        return systems.stream().map(sys -> {
            List<CdsBusiness> businesses = businessMapper.selectList(
                    new LambdaQueryWrapper<CdsBusiness>()
                            .eq(CdsBusiness::getSystemId, sys.getId())
                            .orderByAsc(CdsBusiness::getSortOrder));

            List<BusinessNode> bizNodes = businesses.stream()
                    .map(b -> BusinessNode.builder()
                            .id(b.getId())
                            .name(b.getName())
                            .code(b.getCode())
                            .description(b.getDescription())
                            .sortOrder(b.getSortOrder())
                            .build())
                    .collect(Collectors.toList());

            return SystemResponse.builder()
                    .id(sys.getId())
                    .name(sys.getName())
                    .code(sys.getCode())
                    .description(sys.getDescription())
                    .status(sys.getStatus())
                    .businesses(bizNodes)
                    .build();
        }).collect(Collectors.toList());
    }

    public SystemResponse getByCode(String code) {
        CdsSystem sys = systemMapper.selectOne(
                new LambdaQueryWrapper<CdsSystem>().eq(CdsSystem::getCode, code));
        if (sys == null) return null;
        return listAll().stream()
                .filter(s -> s.getCode().equals(code))
                .findFirst().orElse(null);
    }
}

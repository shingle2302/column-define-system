package com.cds.controller;

import com.cds.dto.PresetResponse;
import com.cds.dto.R;
import com.cds.dto.SystemResponse;
import com.cds.service.ColumnPresetService;
import com.cds.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;
    private final ColumnPresetService presetService;

    /** 获取所有接入系统及业务模块 */
    @GetMapping("/systems")
    public R<List<SystemResponse>> listSystems() {
        return R.ok(systemService.listAll());
    }

    /** 获取指定系统下的所有预设 */
    @GetMapping("/systems/{systemCode}/presets")
    public R<List<PresetResponse>> listPresetsBySystem(@PathVariable String systemCode) {
        return R.ok(presetService.listBySystem(systemCode));
    }

    /** 获取指定业务模块下的预设 */
    @GetMapping("/businesses/{businessId}/presets")
    public R<List<PresetResponse>> listPresetsByBusiness(@PathVariable String businessId) {
        return R.ok(presetService.listByBusiness(businessId));
    }
}

package com.cds.controller;

import com.cds.dto.PresetResponse;
import com.cds.dto.R;
import com.cds.service.ColumnPresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presets")
@RequiredArgsConstructor
public class PresetController {

    private final ColumnPresetService presetService;

    @GetMapping
    public R<List<PresetResponse>> listAll() {
        return R.ok(presetService.listAll());
    }

    @GetMapping("/{id}")
    public R<PresetResponse> getById(@PathVariable String id) {
        try {
            return R.ok(presetService.getById(id));
        } catch (RuntimeException e) {
            return R.notFound(e.getMessage());
        }
    }
}

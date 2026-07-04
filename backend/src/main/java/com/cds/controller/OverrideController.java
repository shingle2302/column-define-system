package com.cds.controller;

import com.cds.dto.OverrideRequest;
import com.cds.dto.OverrideResponse;
import com.cds.dto.R;
import com.cds.service.ColumnOverrideService;
import com.cds.service.ColumnShareService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/overrides")
@RequiredArgsConstructor
public class OverrideController {

    private final ColumnOverrideService overrideService;
    private final ColumnShareService shareService;

    @GetMapping("/{presetId}")
    public R<OverrideResponse> load(@PathVariable String presetId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return R.unauthorized("未认证");
        }
        String role = (String) request.getAttribute("role");
        OverrideResponse override = overrideService.load(presetId, userId, role);
        return R.ok(override);
    }

    @PutMapping("/{presetId}")
    public R<Void> save(@PathVariable String presetId,
                        @RequestBody OverrideRequest body,
                        HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return R.unauthorized("未认证");
        }
        overrideService.save(presetId, userId, body);
        return R.ok();
    }

    @PostMapping("/{presetId}/share")
    public R<Map<String, String>> share(@PathVariable String presetId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return R.unauthorized("未认证");
        }
        String token = shareService.createShare(presetId, userId);
        return R.ok(Map.of("token", token));
    }
}

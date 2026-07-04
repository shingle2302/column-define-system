package com.cds.controller;

import com.cds.dto.R;
import com.cds.dto.SharedResponse;
import com.cds.service.ColumnShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shared")
@RequiredArgsConstructor
public class SharedController {

    private final ColumnShareService shareService;

    @GetMapping("/{token}")
    public R<SharedResponse> loadShared(@PathVariable String token) {
        try {
            return R.ok(shareService.loadShared(token));
        } catch (RuntimeException e) {
            return R.notFound(e.getMessage());
        }
    }
}

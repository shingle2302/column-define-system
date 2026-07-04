package com.cds.controller;

import com.cds.config.JwtUtil;
import com.cds.dto.LoginRequest;
import com.cds.dto.LoginResponse;
import com.cds.dto.R;
import com.cds.entity.SysUser;
import com.cds.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            SysUser user = sysUserService.authenticate(request.getUsername(), request.getPassword());
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getSystemId());

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(), user.getUsername(), user.getDisplayName(), user.getRole(), user.getSystemId());

            return R.ok(new LoginResponse(token, userInfo));
        } catch (RuntimeException e) {
            return R.fail(401, e.getMessage());
        }
    }
}

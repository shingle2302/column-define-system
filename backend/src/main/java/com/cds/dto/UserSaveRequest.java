package com.cds.dto;

import lombok.Data;

@Data
public class UserSaveRequest {
    private String username;
    private String password;
    private String displayName;
    private String role;
    private String systemId;
}

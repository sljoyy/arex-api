package com.arextest.report.model.api.contracts.filesystem;

import lombok.Data;

@Data
public class ValidInvitationRequestType {
    private String email;
    private String workspaceId;
    private String token;
}
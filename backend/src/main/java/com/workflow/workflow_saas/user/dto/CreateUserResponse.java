package com.workflow.workflow_saas.user.dto;

import com.workflow.workflow_saas.user.User;

public record CreateUserResponse(Long id, String name, String email, User.AccountState accountState) {

}

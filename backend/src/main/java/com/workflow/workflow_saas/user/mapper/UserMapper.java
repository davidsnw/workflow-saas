package com.workflow.workflow_saas.user.mapper;

import com.workflow.workflow_saas.user.User;
import com.workflow.workflow_saas.user.dto.CreateUserResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static CreateUserResponse toCreateUserResponse(User user) {
        return new CreateUserResponse(user.getId(),user.getName(), user.getEmail(),user.getAccountState());
    }

}

package com.workflow.workflow_saas.user;

import com.workflow.workflow_saas.role.Role;
import com.workflow.workflow_saas.role.RoleName;
import com.workflow.workflow_saas.tenant.Tenant;
import com.workflow.workflow_saas.user.exception.DuplicateRoleException;
import com.workflow.workflow_saas.user.exception.RoleNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void assignIllegalRoleTest(){
        User testUser = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "432141213", User.AccountState.INACTIVE);
        Assertions.assertThrows(IllegalArgumentException.class, () -> testUser.addRole(null));
        testUser.addRole(new Role(RoleName.EMPLOYEE));
        Assertions.assertThrows(DuplicateRoleException.class, () -> testUser.addRole(new Role(RoleName.EMPLOYEE)));
    }

    @Test
    public void removeUnassignedRoleTest(){
        User testUser = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "432141213", User.AccountState.INACTIVE);
        Role role = new Role(RoleName.EMPLOYEE);
        Assertions.assertThrows(RoleNotFoundException.class, () -> testUser.removeRole(role));
    }

    @Test
    public void checkExistentRolesTest(){
        User testUser = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "432141213", User.AccountState.INACTIVE);
        Role role = new Role(RoleName.EMPLOYEE);
        testUser.addRole(role);
        Assertions.assertTrue(testUser.getRoles().contains(role));
        testUser.removeRole(role);
        Assertions.assertFalse(testUser.getRoles().contains(role));
    }

}

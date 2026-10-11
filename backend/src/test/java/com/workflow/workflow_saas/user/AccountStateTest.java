package com.workflow.workflow_saas.user;

import com.workflow.workflow_saas.tenant.Tenant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountStateTest {

    @Test
    public void assignIllegalStateTest(){
        User testUserInactive = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "432141213", AccountState.INACTIVE);
        User testUserActive = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "432141213", AccountState.ACTIVE);
        Assertions.assertThrows(IllegalStateException.class, testUserActive::activateUser);
        Assertions.assertThrows(IllegalStateException.class, testUserInactive::deactivateUser);
    }

    @Test
    public void deactivateUserTest(){
        User testUser = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "432141213", AccountState.ACTIVE);
        testUser.deactivateUser();
        Assertions.assertEquals(AccountState.INACTIVE,testUser.getAccountState());
    }

    @Test
    public void activateUserTest(){
        User testUser = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "432141213", AccountState.INACTIVE);
        testUser.activateUser();
        Assertions.assertEquals(AccountState.ACTIVE,testUser.getAccountState());
    }



}

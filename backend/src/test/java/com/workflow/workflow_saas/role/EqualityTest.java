package com.workflow.workflow_saas.role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class EqualityTest {
    @Test
    public void equalRolesTest() {
        Role role1 = new Role(RoleName.TECHNICIAN);
        Role role2 = new Role(RoleName.TECHNICIAN);

        Assertions.assertEquals(role1, role2);
        Assertions.assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    public void differentRolesTest() {
        Role role1 = new Role(RoleName.TECHNICIAN);
        Role role2 = new Role(RoleName.EMPLOYEE);

        Assertions.assertNotEquals(role1, role2);
    }
}

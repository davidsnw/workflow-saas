package com.workflow.workflow_saas.role;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvalidDataTest {

    @Test
    public void nullRoleNameTest(){
        Assertions.assertThrows(NullPointerException.class, () -> new Role(null));
    }
}

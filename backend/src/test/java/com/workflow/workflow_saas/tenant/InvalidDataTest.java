package com.workflow.workflow_saas.tenant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class InvalidDataTest {

    @Test
    public void nullNameTest(){
        Assertions.assertThrows(NullPointerException.class, () -> new Tenant(null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "  ", "   ", "\t", "\n", "\r", "\t\n\r", "\u2003"})
    public void blankNameTest(String name) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Tenant(name)
        );
    }

    @Test
    public void assignNullNameToTenantTest(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Tenant("Test tenant").rename(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "   ", "\t", "\n", "\r", "\t\n\r", "\u2003"})
    public void assignBlankNameToTenantTest(String name) {
        Tenant tenant = new Tenant("Test tenant");

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> tenant.rename(name)
        );
    }
}

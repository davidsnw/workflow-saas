package com.workflow.workflow_saas.customer;

import com.workflow.workflow_saas.tenant.Tenant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class InvalidDataTest {

    @Test
    public void nullTenantTest(){
        Assertions.assertThrows(NullPointerException.class, ()-> new Customer("Test customer",null));
    }

    @Test
    public void nullNameTest(){
        Assertions.assertThrows(NullPointerException.class, ()-> new Customer(null,new Tenant("Test tenant")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", " ", "  ", "   ", "\t", "\n", "\r", "\t\n\r", "\u2003"
    })
    public void blankNameTest(String name) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Customer(name,new Tenant("Test tenant"))
        );
    }

    @Test
    public void renameCustomerWithNullNameTest(){
        Customer customer = new Customer("Test customer", new Tenant("Test tenant"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> customer.rename(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", " ", "  ", "   ", "\t", "\n", "\r", "\t\n\r", "\u2003"
    })
    public void renameCustomerWithBlankNameTest(String name){
        Customer customer = new Customer("Test customer", new Tenant("Test tenant"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> customer.rename(name));
    }

    @Test
    public void renameCustomerTest(){
        Customer customer = new Customer("Test customer",new Tenant("Test tenant"));
        customer.rename("TEST");
        Assertions.assertEquals("TEST",customer.getName());
    }

    @Test
    public void debugBlankCustomerNameTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Customer("", new Tenant("Test tenant"))
        );
    }


}

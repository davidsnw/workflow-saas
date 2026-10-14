package com.workflow.workflow_saas.user;
import com.workflow.workflow_saas.tenant.Tenant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


public class InvalidDataTest {

    @Test
    public void nullTenantTest() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new User(null, "test user", "testuser@gmail.com", "somePasswordHash", User.AccountState.ACTIVE));
    }

    @Test
    public void nullNameTest() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new User(new Tenant("Test Tenant"), null, "testuser@gmail.com", "somePasswordHash", User.AccountState.ACTIVE));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", " ", "  ", "   ", "\t", "\n", "\r", "\t\n\r", "\u2003"
    })
    public void blankNameTest(String name) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        new Tenant("Test Tenant"),
                        name,
                        "testuser@gmail.com",
                        "somePasswordHash",
                        User.AccountState.ACTIVE
                )
        );
    }

    @Test
    public void nullEmailTest() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new User(new Tenant("Test Tenant"), "Test user", null, "somePasswordHash", User.AccountState.ACTIVE));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", " ", "  ", "   ", "\t", "\n", "\r", "\t\n\r", "\u2003"
    })
    public void blankEmailTest(String email) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        new Tenant("Test Tenant"),
                        "Test user",
                        email,
                        "somePasswordHash",
                        User.AccountState.ACTIVE
                )
        );
    }


    @Test
    public void nullPasswordHashTest() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", null, User.AccountState.ACTIVE));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", " ", "  ", "   ", "\t", "\n", "\r", "\t\n\r", "\u2003"
    })
    public void blankPasswordHashTest(String passwordHash) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        new Tenant("Test Tenant"),
                        "Test user",
                        "testuser@gmail.com",
                        passwordHash,
                        User.AccountState.ACTIVE
                )
        );
    }

    @Test
    public void nullAccountStateTest() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "somePasswordHash", null));
    }


    @Test
    public void assignNullNameToUserTest(){
        User testUser = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "somePasswordHash", User.AccountState.INACTIVE);
        Assertions.assertThrows(IllegalArgumentException.class,() -> testUser.rename(null));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "", " ", "\t", "\n", "\r", "\t\n\r", "\u2003"
    })
    public void assignBlankNameToUserTest(String name){
        User testUser = new User(new Tenant("Test Tenant"), "test user", "testuser@gmail.com", "somePasswordHash", User.AccountState.INACTIVE);
        Assertions.assertThrows(IllegalArgumentException.class,() -> testUser.rename(name));
    }

    @Test
    public void renameUserTest() {
        User testUser = new User(
                new Tenant("Test Tenant"),
                "Old name",
                "testuser@gmail.com",
                "somePasswordHash",
                User.AccountState.ACTIVE
        );

        testUser.rename("New name");

        Assertions.assertEquals("New name", testUser.getName());
    }

}

package com.workflow.workflow_saas.user_service;

import com.workflow.workflow_saas.tenant.Tenant;
import com.workflow.workflow_saas.tenant.TenantRepository;
import com.workflow.workflow_saas.user.User;
import com.workflow.workflow_saas.user.UserRepository;
import com.workflow.workflow_saas.user.UserService;
import com.workflow.workflow_saas.user.dto.CreateUserRequest;
import com.workflow.workflow_saas.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void createUserWhenEmailAlreadyExists(){

        String email="test@gmail.com";

        when(userRepository.existsByEmail(email)).thenReturn(true);
        CreateUserRequest request = new CreateUserRequest(1L,"Test user",email,"Some password");
        Assertions.assertThrows(EmailAlreadyExistsException.class, ()-> userService.createUser(request));
        verify(userRepository).existsByEmail(email);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(tenantRepository, passwordEncoder);
    }

    @Test
    public void createUserSuccessfully(){
        Long tenantId = 1L;
        String email = "test@gmail.com";
        String rawPassword = "Password123!";
        String encodedPassword = "encoded-password";

        Tenant tenant = new Tenant("Test tenant");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        CreateUserRequest request = new CreateUserRequest(tenantId, "Test user", email, rawPassword);

        User result = userService.createUser(request);

        verify(userRepository).existsByEmail(email);
        verify(tenantRepository).findById(tenantId);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));

        Assertions.assertEquals(email,result.getEmail());
        Assertions.assertEquals(encodedPassword,result.getPasswordHash());
        Assertions.assertEquals(tenant,result.getTenant());
    }

}

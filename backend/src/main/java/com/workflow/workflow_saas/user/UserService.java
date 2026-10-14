package com.workflow.workflow_saas.user;

import com.workflow.workflow_saas.tenant.Tenant;
import com.workflow.workflow_saas.tenant.TenantRepository;
import com.workflow.workflow_saas.tenant.exception.TenantNotFoundException;
import com.workflow.workflow_saas.user.dto.CreateUserRequest;
import com.workflow.workflow_saas.user.exception.EmailAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       TenantRepository tenantRepository,
                       PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new EmailAlreadyExistsException("Email already exists! Choose another email");
        }
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new TenantNotFoundException("Tenant doesn't exist"));
        String passwordHash= passwordEncoder.encode(request.password());
        User user = new User(tenant,request.name(),request.email(),passwordHash, User.AccountState.ACTIVE);
        return userRepository.save(user);
    }
}

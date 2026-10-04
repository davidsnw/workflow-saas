package com.workflow.workflow_saas;

import com.workflow.workflow_saas.customer.Customer;
import com.workflow.workflow_saas.customer.CustomerRepository;
import com.workflow.workflow_saas.role.Role;
import com.workflow.workflow_saas.role.RoleName;
import com.workflow.workflow_saas.role.RoleRepository;
import com.workflow.workflow_saas.tenant.Tenant;
import com.workflow.workflow_saas.tenant.TenantRepository;
import com.workflow.workflow_saas.user.AccountState;
import com.workflow.workflow_saas.user.DuplicateRoleException;
import com.workflow.workflow_saas.user.User;
import com.workflow.workflow_saas.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class WorkflowSaasApplicationTests {

	private final TenantRepository tenantRepository;
	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public WorkflowSaasApplicationTests(TenantRepository tenantRepository,
										CustomerRepository customerRepository,
										UserRepository userRepository,
										RoleRepository roleRepository) {
		this.tenantRepository = tenantRepository;
		this.customerRepository = customerRepository;
		this.userRepository =userRepository;
		this.roleRepository = roleRepository;
	}

		@Test
		void entityAuditableTest() {
		Tenant tenant = new Tenant("Test tenant");
		Tenant savedTenant = tenantRepository.save(tenant);
		Instant firstTimestampForTenant = savedTenant.getUpdatedAt();
		tenant.rename("Another tenant");
		Tenant updatedTenant = tenantRepository.saveAndFlush(tenant);
		Instant secondTimestampForTenant = updatedTenant.getUpdatedAt();
		Assertions.assertNotEquals(firstTimestampForTenant,secondTimestampForTenant);

		Customer customer = new Customer("First customer", savedTenant);
		Customer savedCustomer = customerRepository.save(customer);
		Instant firstTimestampForCustomer = savedCustomer.getUpdatedAt();
		savedCustomer.rename("Just a customer");
		Customer updatedCustomer = customerRepository.saveAndFlush(savedCustomer);
		Instant secondTimestampForCustomer = updatedCustomer.getUpdatedAt();
		Assertions.assertNotEquals(firstTimestampForCustomer,secondTimestampForCustomer);
		}

		@Test
		void customerBelongsToTenant(){
			Tenant tenant = new Tenant("Test tenant");
			Tenant savedTenant = tenantRepository.save(tenant);
			Customer customer = new Customer("First customer",savedTenant);
			Customer savedCustomer = customerRepository.save(customer);
			Assertions.assertNotNull(savedCustomer.getId());
			Assertions.assertNotNull(savedCustomer.getTenant());
			Assertions.assertEquals(savedCustomer.getTenant().getId(),savedTenant.getId());
		}

		@Test
		void userPersistenceAndAuditing(){
		Tenant tenant = new Tenant("Test tenant");
		Tenant savedTenant = tenantRepository.save(tenant);
		User user = new User(savedTenant,"Test user","usertesting@gmail.com","jhkgfsdu54u64k", AccountState.ACTIVE);
		User savedUser = userRepository.save(user);
		Assertions.assertNotNull(savedUser.getId());
		Assertions.assertNotNull(savedUser.getUpdatedAt());
		Instant firstUpdatedAtValue = savedUser.getUpdatedAt();
		savedUser.rename("Still a test user");
		savedUser = userRepository.saveAndFlush(savedUser);
		Instant secondUpdatedAtValue = savedUser.getUpdatedAt();
		Assertions.assertNotEquals(firstUpdatedAtValue,secondUpdatedAtValue);
		}

		@Test
		@Transactional
		void roleRetrievingAndAdding(){
		Optional<Role> technician = roleRepository.findByRoleName(RoleName.TECHNICIAN);
		Assertions.assertTrue(technician.isPresent());
		Tenant tenant = new Tenant("Test tenant");
		Tenant savedTenant = tenantRepository.save(tenant);
		User user = new User(savedTenant,"Test user","usertesting@gmail.com","jhkgfsdu54u64k", AccountState.ACTIVE);
		User savedUser = userRepository.save(user);
		Role technicianRole = technician.get();
		savedUser.addRole(technicianRole);
		savedUser = userRepository.saveAndFlush(savedUser);
		User reloadedUser = userRepository.findById(savedUser.getId()).orElseThrow();
		Assertions.assertTrue(reloadedUser.getRoles().contains(technicianRole));
		Optional<Role>employee = roleRepository.findByRoleName(RoleName.EMPLOYEE);
		Role employeeRole = employee.orElseThrow();
		reloadedUser.addRole(employeeRole);
		reloadedUser = userRepository.saveAndFlush(reloadedUser);
		User flushedUser = userRepository.saveAndFlush(reloadedUser);
		Assertions.assertEquals(2,reloadedUser.getRoles().size());
		Assertions.assertThrows(DuplicateRoleException.class, () -> flushedUser.addRole(technicianRole));
		}
	}


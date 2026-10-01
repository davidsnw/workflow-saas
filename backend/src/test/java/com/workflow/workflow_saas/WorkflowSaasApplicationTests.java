package com.workflow.workflow_saas;

import com.workflow.workflow_saas.customer.Customer;
import com.workflow.workflow_saas.customer.CustomerRepository;
import com.workflow.workflow_saas.tenant.Tenant;
import com.workflow.workflow_saas.tenant.TenantRepository;
import com.workflow.workflow_saas.user.AccountState;
import com.workflow.workflow_saas.user.User;
import com.workflow.workflow_saas.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class WorkflowSaasApplicationTests {

	private final TenantRepository tenantRepository;
	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;

	public WorkflowSaasApplicationTests(TenantRepository tenantRepository,
										CustomerRepository customerRepository,
										UserRepository userRepository) {
		this.tenantRepository = tenantRepository;
		this.customerRepository = customerRepository;
		this.userRepository =userRepository;
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
	}


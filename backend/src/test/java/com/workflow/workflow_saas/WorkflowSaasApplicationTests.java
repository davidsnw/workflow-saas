package com.workflow.workflow_saas;

import com.workflow.workflow_saas.customer.Customer;
import com.workflow.workflow_saas.customer.CustomerRepository;
import com.workflow.workflow_saas.tenant.Tenant;
import com.workflow.workflow_saas.tenant.TenantRepository;
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

	public WorkflowSaasApplicationTests(TenantRepository tenantRepository,CustomerRepository customerRepository) {
		this.tenantRepository = tenantRepository;
		this.customerRepository = customerRepository;
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
	}


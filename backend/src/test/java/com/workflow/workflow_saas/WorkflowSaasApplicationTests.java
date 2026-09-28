package com.workflow.workflow_saas;

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

	//private final TenantRepository tenantRepository;

	public WorkflowSaasApplicationTests(TenantRepository tenantRepository) {
		//this.tenantRepository = tenantRepository;
	}

		@Test
		void entityAuditableTest() {
//		Tenant tenant = new Tenant("Test tenant");
//		tenantRepository.save(
//				tenant);
//		Instant firstValue = tenant.getUpdatedAt();
//		tenant.rename("another tenant");
//		Tenant updatedTenant = tenantRepository.saveAndFlush(tenant);
//		Instant secondValue = updatedTenant.getUpdatedAt();
//			System.out.println("FIRST:  " + firstValue);
//			System.out.println("SECOND: " + secondValue);
//		Assertions.assertNotEquals(firstValue,secondValue);
		}
	}


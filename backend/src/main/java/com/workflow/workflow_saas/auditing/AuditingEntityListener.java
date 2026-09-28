package com.workflow.workflow_saas.auditing;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

public class AuditingEntityListener {

    @PrePersist
    public void createTimestamp(Auditable entity) {
        //System.out.println("BEFORE: " + entity.getUpdatedAt());
        entity.setUpdatedAt(Instant.now());
        //System.out.println("AFTER: " + entity.getUpdatedAt());
    }

    @PreUpdate
    public void updateTimestamp(Auditable entity) {
        //System.out.println("BEFORE UPDATE: " + entity.getUpdatedAt());
        Instant now = Instant.now();
        //System.out.println("NOW: " + now);
        entity.setUpdatedAt(now);
        //System.out.println("AFTER UPDATE: " + entity.getUpdatedAt());
    }
}

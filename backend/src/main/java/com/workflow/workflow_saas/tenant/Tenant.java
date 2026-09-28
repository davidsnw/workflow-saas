package com.workflow.workflow_saas.tenant;

import com.workflow.workflow_saas.auditing.Auditable;
import com.workflow.workflow_saas.auditing.AuditingEntityListener;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;

import java.time.Instant;


@Entity
@Table(name="tenants")
@EntityListeners(AuditingEntityListener.class)
public class Tenant implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 255)
    private String name;

    @Generated
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at",nullable = false)
    private Instant updatedAt;

    protected Tenant() {

    }

    public Tenant(String name) {
        this.name = name;
    }

    public Long getId(){
        return this.id;
    }

    @Override
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    @Override
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void rename(String name) {
        this.name = name;
    }
}

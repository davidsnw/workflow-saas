package com.workflow.workflow_saas.user;

import com.workflow.workflow_saas.auditing.Auditable;
import com.workflow.workflow_saas.auditing.AuditingEntityListener;
import com.workflow.workflow_saas.tenant.Tenant;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="users")
@EntityListeners(AuditingEntityListener.class)
public class User implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id",nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name="password_hash",nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name="account_state",nullable = false)
    private AccountState accountState;

    @Column(name = "created_at",nullable = false,insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at",nullable = false)
    private Instant updatedAt;

    protected User(){

    }

    public User(Tenant tenant, String name, String email, String passwordHash, AccountState accountState){
        this.tenant =tenant;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountState = accountState;
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

    public Long getId(){
        return this.id;
    }

    public void rename(String name){
        this.name = name;
    }
}

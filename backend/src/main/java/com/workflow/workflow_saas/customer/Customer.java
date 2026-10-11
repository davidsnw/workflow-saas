package com.workflow.workflow_saas.customer;

import com.workflow.workflow_saas.auditing.Auditable;
import com.workflow.workflow_saas.auditing.AuditingEntityListener;
import com.workflow.workflow_saas.tenant.Tenant;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class Customer implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="tenant_id",nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 255)
    private String name;

    @Generated
    @Column(name="created_at",nullable = false,insertable = false,updatable = false)
    private Instant createdAt;

    @Column(name="updated_at",nullable = false)
    private Instant updatedAt;


    protected Customer (){

    }

    public Customer(String name, Tenant tenant){
        Objects.requireNonNull(name,"Customer name cannot be null!");
        if(name.isBlank()){
            throw new IllegalArgumentException("Customer name cannot be blank!");
        }
        this.name = name;
        Objects.requireNonNull(tenant,"Tenant cannot be null!");
        this.tenant = tenant;
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

    public Tenant getTenant(){
        return this.tenant;
    }
    public String getName(){
        return this.name;
    }

    public void rename(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        this.name = name;
    }
}

package com.workflow.workflow_saas.user;

import com.workflow.workflow_saas.auditing.Auditable;
import com.workflow.workflow_saas.auditing.AuditingEntityListener;
import com.workflow.workflow_saas.role.Role;
import com.workflow.workflow_saas.tenant.Tenant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<Role> roles = new HashSet<>();

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

    public User(Tenant tenant, String name, String email, String passwordHash, AccountState accountState) {
        this.tenant = Objects.requireNonNull(tenant, "Tenant cannot be null");

        Objects.requireNonNull(name, "User name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be blank!");
        }
        this.name = name;

        Objects.requireNonNull(email, "E-mail cannot be null");
        if (email.isBlank()) {
            throw new IllegalArgumentException("E-mail cannot be blank!");
        }
        this.email = email;

        Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be blank!");
        }
        this.passwordHash = passwordHash;

        this.accountState = Objects.requireNonNull(accountState, "Account state cannot be null");
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

    public String getName(){
        return this.name;
    }

    public Set<Role> getRoles(){
        return this.roles;
    }

    public AccountState getAccountState(){
        return this.accountState;
    }

    public void addRole(Role role){
        if(role == null){
            throw new IllegalArgumentException("Role cannot be null!");
        }
        if(this.roles.contains(role)){
            throw new DuplicateRoleException("This role is already assigned!");
        }
        this.roles.add(role);
    }

    public void removeRole(Role role){
        if(!this.roles.contains(role)){
            throw new RoleNotFoundException("Role not found");
        }
        this.roles.remove(role);
    }

    public void rename(String name){
        if(name == null){
            throw new IllegalArgumentException("Name cannot be null!");
        }
        if(name.isBlank()){
            throw new IllegalArgumentException("Name cannot be blank!");
        }
        this.name = name;
    }

    public void activateUser(){
        if(this.accountState.equals(AccountState.ACTIVE)){
            throw new IllegalStateException("The user is already active");
        }
            this.accountState = AccountState.ACTIVE;
    }

    public void deactivateUser(){
        if(this.accountState.equals(AccountState.INACTIVE)){
            throw new IllegalStateException("The user is already inactive");
        }
        this.accountState = AccountState.INACTIVE;
    }
}


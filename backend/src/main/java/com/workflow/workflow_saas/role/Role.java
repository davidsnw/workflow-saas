package com.workflow.workflow_saas.role;

import jakarta.persistence.*;

@Entity
@Table(name="roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name",nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    protected Role(){

    }

    public Role(RoleName roleName){
        this.roleName = roleName;
    }

    public Long getId() {
        return id;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if(!(o instanceof Role)){
            return false;
        }
        Role role = (Role)o;

        return this.roleName.equals(role.getRoleName());
    }

    @Override
    public int hashCode(){
        return roleName.hashCode();
    }
}

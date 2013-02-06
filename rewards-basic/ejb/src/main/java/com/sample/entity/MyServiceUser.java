package com.sample.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

/**
 * jBPMとは独立したサービスのユーザ、と想定したエンティティ
 */
@Entity
public class MyServiceUser {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private short version;

    @Column(unique = true)
    private String name;

    @ManyToOne
    private MyServiceGroup group;

    public MyServiceUser() {
    }

    public MyServiceUser(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyServiceGroup getGroup() {
        return group;
    }

    public void setGroup(MyServiceGroup group) {
        this.group = group;
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MyServiceUser)) {
            return false;
        }

        if (id != null) {
            MyServiceUser mc = (MyServiceUser) obj;
            return id.equals(mc.id);
        }
        return super.equals(obj);
    }

}

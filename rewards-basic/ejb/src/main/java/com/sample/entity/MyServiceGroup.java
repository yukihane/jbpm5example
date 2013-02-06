package com.sample.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * jBPMとは独立したサービスのグループ、と想定したエンティティ
 */
@Entity
public class MyServiceGroup {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private short version;

    @Column(unique = true)
    private String name;

    public MyServiceGroup() {
    }

    public MyServiceGroup(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!(obj instanceof MyServiceGroup)) {
            return false;
        }

        if (id != null) {
            MyServiceGroup mc = (MyServiceGroup) obj;
            return id.equals(mc.id);
        }
        return super.equals(obj);
    }

}

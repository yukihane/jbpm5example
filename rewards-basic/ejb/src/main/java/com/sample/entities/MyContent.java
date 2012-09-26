package com.sample.entities;

import java.io.Serializable;
import javax.persistence.*;

@Entity
public class MyContent implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Version
    private long version;

    private String contentMessage;

    public String getContentMessage() {
        return contentMessage;
    }

    public void setContentMessage(String contentMessage) {
        this.contentMessage = contentMessage;
    }

}

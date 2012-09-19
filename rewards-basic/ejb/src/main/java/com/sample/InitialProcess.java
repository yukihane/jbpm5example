package com.sample;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.drools.SystemEventListenerFactory;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskServiceSession;

@Startup
@Singleton
public class InitialProcess {

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(
                emf, SystemEventListenerFactory.getSystemEventListener());

        TaskServiceSession taskSession = taskService.createSession();
        taskSession.addUser(new User("Administrator"));
        taskSession.addUser(new User("john"));
        taskSession.addUser(new User("mary"));
    }
}

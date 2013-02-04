package com.sample;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;

@Singleton
@Startup
public class MyKnowledgeBase {

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    private KnowledgeBase kbase;

    @PostConstruct
    public void init() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kbuilder.add(
                ResourceFactory.newClassPathResource("rewards-basic.bpmn"),
                ResourceType.BPMN2);
        kbase = kbuilder.newKnowledgeBase();
    }

    private StatefulKnowledgeSession createKnowledgeSession() {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        StatefulKnowledgeSession ksession = JPAKnowledgeService
                .newStatefulKnowledgeSession(kbase, null, env);

        new JPAWorkingMemoryDbLogger(ksession);

        return ksession;
    }

    public ClientSession createSession() {
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        TaskService taskService = new TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService taskClient = new LocalTaskService(taskService);

        LocalHTWorkItemHandler humanTaskHandler = new LocalHTWorkItemHandler(
                taskClient, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                humanTaskHandler);

        return new ClientSession(taskClient, ksession);
    }

    public static class ClientSession {
        private final LocalTaskService taskClient;
        private final StatefulKnowledgeSession knowledgeSession;

        private ClientSession(LocalTaskService taskClient,
                StatefulKnowledgeSession ksession) {
            this.taskClient = taskClient;
            this.knowledgeSession = ksession;
        }

        public LocalTaskService getTaskClient() {
            return taskClient;
        }

        public StatefulKnowledgeSession getKnowledgeSession() {
            return knowledgeSession;
        }
    }
}

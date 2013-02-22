package com.sample;

import java.util.Map;

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
import org.drools.runtime.KnowledgeRuntime;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
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
                ResourceFactory.newClassPathResource("rewards-basic.bpmn2"),
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

    public CommunicationPath createCommunicationPath() {
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        TaskService taskService = new TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService lts = new LocalTaskService(taskService);

        LocalHTWorkItemHandler humanTaskHandler = new MyHandler(lts, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                humanTaskHandler);

        return new CommunicationPath(lts, ksession);
    }

    public static class CommunicationPath {
        private final LocalTaskService taskService;
        private final StatefulKnowledgeSession knowledgeSession;

        private CommunicationPath(LocalTaskService taskService,
                StatefulKnowledgeSession ksession) {
            this.taskService = taskService;
            this.knowledgeSession = ksession;
        }

        public LocalTaskService getTaskService() {
            return taskService;
        }

        public StatefulKnowledgeSession getKnowledgeSession() {
            return knowledgeSession;
        }
    }

    private class MyHandler extends LocalHTWorkItemHandler {

        public MyHandler(org.jbpm.task.TaskService client,
                KnowledgeRuntime session) {
            super(client, session);
        }

        @Override
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            Map<String, Object> params = workItem.getParameters();
            System.out.println("params: " + params);
            Map<String, Object> results = workItem.getResults();
            System.out.println("results: " + results);
            super.executeWorkItem(workItem, manager);
        }
    }
}

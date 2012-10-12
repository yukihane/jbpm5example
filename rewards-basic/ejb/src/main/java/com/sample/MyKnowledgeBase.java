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

    public CommunicationPath createCommunicationPath() {
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        TaskService taskService = new TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService lts = new LocalTaskService(taskService);

        // deprecated だけれども5.3.0では代替クラスが無いので仕方ない
        // http://stackoverflow.com/questions/10815779/local-human-task-service-jbpm
        // https://community.jboss.org/thread/204619
        // 5.4.0ではLocalHTWorkItemHandlerというものがあるのでこれを使う
        LocalHTWorkItemHandler humanTaskHandler = new LocalHTWorkItemHandler(
                lts, ksession);
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
}

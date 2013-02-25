package com.sample;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;
import org.kie.KieBase;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.SystemEventListenerFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;
import org.kie.persistence.jpa.JPAKnowledgeService;
import org.kie.runtime.Environment;
import org.kie.runtime.StatefulKnowledgeSession;

import com.sample.workitem.MyWorkItemHandler;

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
        // FIXME
        // knowledge-apiとkie-apiそれぞれ別のorg.kie.runtime.EnvironmentNameが定義されている
        // env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set("org.kie.persistence.jpa.EntityManagerFactory", emf);
        env.set("drools.persistence.jpa.EntityManagerFactory", emf);

        // FIXME 現時点でのsnapshotバージョンではKieBaseにキャストする必要がある？
        // https://issues.jboss.org/browse/DROOLS-34 で解消？
        StatefulKnowledgeSession ksession = JPAKnowledgeService
                .newStatefulKnowledgeSession((KieBase) kbase, null, env);

        new JPAWorkingMemoryDbLogger(ksession);

        return ksession;
    }

    public CommunicationPath createCommunicationPath() {
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        TaskService taskService = new TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService lts = new LocalTaskService(taskService);

        LocalHTWorkItemHandler humanTaskHandler = new MyWorkItemHandler(lts, ksession);
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

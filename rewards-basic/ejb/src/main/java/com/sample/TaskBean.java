package com.sample;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
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
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

@Stateless
public class TaskBean implements TaskLocal {

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    @Inject
    private MyKnowledgeBase myKnowledgeBase;

    public List<TaskSummary> retrieveTaskList(String actorId) throws Exception {

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        TaskService localTaskService = getTaskService(ksession);

        List<TaskSummary> list = localTaskService
                .getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        System.out.println("retrieveTaskList by " + actorId);
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());
        }

        return list;
    }

    public void approveTask(String actorId, long taskId) throws Exception {

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        TaskService localTaskService = getTaskService(ksession);

        System.out.println("approveTask (taskId = " + taskId + ") by " + actorId);
        localTaskService.start(taskId, actorId);
        localTaskService.complete(taskId, actorId, null);
        
        return;
    }

    private StatefulKnowledgeSession createKnowledgeSession() {
        StatefulKnowledgeSession ksession = myKnowledgeBase.createKnowledgeSession();

        return ksession;
    }

    public TaskService getTaskService(StatefulKnowledgeSession ksession) {

        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(
                emf, SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);

        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(
                localTaskService, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                humanTaskHandler);

        return localTaskService;
    }
}

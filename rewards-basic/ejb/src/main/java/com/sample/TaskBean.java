package com.sample;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

import com.sample.MyKnowledgeBase.ClientSession;

@Stateless
public class TaskBean implements TaskLocal {

    @Inject
    private MyKnowledgeBase myKnowledgeBase;

    public List<TaskSummary> retrieveTaskList(String actorId) throws Exception {

        final ClientSession session = myKnowledgeBase.createSession();
        registerHandler(session);

        final TaskService taskClient = session.getTaskClient();
        List<TaskSummary> list = taskClient
                .getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        System.out.println("retrieveTaskList by " + actorId);
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());
        }

        return list;
    }

    public void approveTask(String actorId, long taskId) throws Exception {
        final ClientSession session = myKnowledgeBase.createSession();
        registerHandler(session);

        final TaskService taskClient = session.getTaskClient();

        System.out.println("approveTask (taskId = " + taskId + ") by " + actorId);
        taskClient.start(taskId, actorId);
        taskClient.complete(taskId, actorId, null);
        
        return;
    }
    
    private static void registerHandler(ClientSession session){
        final LocalTaskService taskClient = session.getTaskClient();
        final StatefulKnowledgeSession ksession = session.getKnowledgeSession();

        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(
                taskClient, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                humanTaskHandler);
    }
}

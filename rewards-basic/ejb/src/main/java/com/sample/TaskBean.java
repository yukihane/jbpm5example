package com.sample;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;

@Stateless
public class TaskBean implements TaskLocal {

    @Inject
    private MyKnowledgeBase myKnowledgeBase;

    public List<TaskSummary> retrieveTaskList(String actorId) throws Exception {

        TaskService localTaskService = myKnowledgeBase.createSession().getTaskClient();

        List<TaskSummary> list = localTaskService
                .getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        System.out.println("retrieveTaskList by " + actorId);
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());
        }

        return list;
    }

    public void approveTask(String actorId, long taskId) throws Exception {

        TaskService localTaskService = myKnowledgeBase.createSession().getTaskClient();

        System.out.println("approveTask (taskId = " + taskId + ") by " + actorId);
        localTaskService.start(taskId, actorId);
        localTaskService.complete(taskId, actorId, null);
        
        return;
    }
}

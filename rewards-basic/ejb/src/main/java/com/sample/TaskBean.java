package com.sample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jbpm.task.Content;
import org.jbpm.task.Task;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;

import com.sample.entity.MyContent;

@Stateless
public class TaskBean implements TaskLocal {

    @PersistenceContext(unitName = "org.jbpm.persistence.jpa")
    EntityManager em;

    @Inject
    private MyKnowledgeBase myKnowledgeBase;

    @Override
    public List<MyTask> retrieveTaskList(String actorId) {

        TaskService taskService = myKnowledgeBase.createCommunicationPath().getTaskService();

        List<TaskSummary> list = taskService
                .getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        System.out.println("retrieveTaskList by " + actorId);
        List<MyTask> tasks = new ArrayList<MyTask>();
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());

            final Task t = taskService.getTask(task.getId());
            final long contentId = t.getTaskData().getDocumentContentId();
            final Content cont = taskService.getContent(contentId);
            final long myContId = new BigInteger(cont.getContent()).longValue();
            final MyContent myCont = em.find(MyContent.class, myContId);
            tasks.add(new MyTask(task, myCont));
        }

        return tasks;
    }

    public static class MyTask {
        private final TaskSummary task;
        private final MyContent content;

        public MyTask(TaskSummary task, MyContent content) {
            this.task = task;
            this.content = content;
        }

        public TaskSummary getTask() {
            return task;
        }

        public MyContent getContent() {
            return content;
        }

    }

    public void approveTask(String actorId, long taskId) throws Exception {

        TaskService taskClient = myKnowledgeBase.createCommunicationPath().getTaskService();

        System.out.println("approveTask (taskId = " + taskId + ") by " + actorId);
        taskClient.start(taskId, actorId);
        taskClient.complete(taskId, actorId, null);
        
        return;
    }
}

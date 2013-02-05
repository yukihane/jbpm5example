package com.sample;

import static com.sample.Constants.MY_CONTENT_ID;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drools.runtime.process.WorkflowProcessInstance;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;

import com.sample.MyKnowledgeBase.CommunicationPath;
import com.sample.entity.MyContent;

@Stateless
public class TaskBean implements TaskLocal {

    @PersistenceContext(unitName = "org.jbpm.persistence.jpa")
    EntityManager em;

    @Inject
    private MyKnowledgeBase myKnowledgeBase;

    @Override
    public List<MyTask> retrieveTaskList(String actorId) {

        final CommunicationPath cp = myKnowledgeBase.createCommunicationPath();
        final TaskService taskService = cp.getTaskService();

        List<TaskSummary> list = taskService
                .getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        System.out.println("retrieveTaskList by " + actorId);
        List<MyTask> tasks = new ArrayList<MyTask>();
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());
            WorkflowProcessInstance pInstance = (WorkflowProcessInstance) cp
                    .getKnowledgeSession().getProcessInstance(
                            task.getProcessInstanceId());

            final long myContId = (Long) pInstance.getVariable(MY_CONTENT_ID);
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

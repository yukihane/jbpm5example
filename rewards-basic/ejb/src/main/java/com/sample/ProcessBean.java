package com.sample;
import static com.sample.Constants.MY_CONTENT_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

import com.sample.MyKnowledgeBase.CommunicationPath;
import com.sample.entity.MyContent;

@Stateless
public class ProcessBean implements ProcessLocal {

    @PersistenceContext(unitName = "org.jbpm.persistence.jpa")
    EntityManager em;

    @Inject
    private MyKnowledgeBase myKnowledgeBase;

    @Override
    public long editContent(String user, String taskIdStr, String message) {

        final CommunicationPath cp = myKnowledgeBase.createCommunicationPath();
        StatefulKnowledgeSession ksession = cp.getKnowledgeSession();
        final LocalTaskService taskService = cp.getTaskService();
        final long processInstanceId;

        if (taskIdStr == null || taskIdStr.isEmpty()) {
            // start a new process instance
            ProcessInstance processInstance = ksession
                    .startProcess("defaultPackage.rewards-basic");
            processInstanceId = processInstance.getId();

            final List<TaskSummary> tasks = taskService
                    .getTasksAssignedAsPotentialOwner(user, "en-UK");

            boolean executed = false;
            for (TaskSummary ts : tasks) {
                if (ts.getProcessInstanceId() == processInstanceId) {
                    final MyContent cont = new MyContent();
                    cont.setMessage(message);
                    em.persist(cont);
                    final long myContentId = cont.getId();
                    final long taskId = ts.getId();
                    executed = true;
                    completeTask(taskService, taskId, user, myContentId);
                    break;
                }
            }

            if (!executed) {
                throw new RuntimeException(
                        "Task is not found. Incorrect user: " + user);
            }

            System.out.println("Process started ... : processInstanceId = "
                    + processInstanceId);
        } else {
            final long taskId = Long.parseLong(taskIdStr);
            processInstanceId = taskService.getTask(taskId).getTaskData().getProcessInstanceId();
            final WorkflowProcessInstance pInstance = (WorkflowProcessInstance) ksession
                    .getProcessInstance(processInstanceId);
            final long myContentId = (Long) pInstance.getVariable(MY_CONTENT_ID);
            final MyContent c = new MyContent();
            c.setId(myContentId);
            final MyContent myContent = em.find(MyContent.class, c);
            myContent.setMessage(message);
            completeTask(taskService, taskId, user, myContentId);
        }
        return processInstanceId;
    }

    private void completeTask(final LocalTaskService taskService,
            long taskId, String user, long contId) {
        final Map<String, Object> variables = new HashMap<String, Object>();
        // myContentId は rewards-basic.bpmn で定義しているプロセスインスタンス変数の名前
        variables.put(MY_CONTENT_ID, contId);

        taskService.start(taskId, user);
        taskService.completeWithResults(taskId, user, variables);
    }
}

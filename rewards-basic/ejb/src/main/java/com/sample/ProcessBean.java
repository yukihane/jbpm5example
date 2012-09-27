package com.sample;

import java.math.BigInteger;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.task.AccessType;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
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
    public long createContent(String user, MyContent cont) {

        final CommunicationPath cp = myKnowledgeBase.createCommunicationPath();
        StatefulKnowledgeSession ksession = cp.getKnowledgeSession();

        // start a new process instance
        ProcessInstance processInstance = ksession
                .startProcess("com.sample.rewards-basic");
        long processInstanceId = processInstance.getId();

        final LocalTaskService taskService = cp.getTaskService();
        final List<TaskSummary> tasks = taskService
                .getTasksAssignedAsPotentialOwner(user, "en-UK");

        boolean executed = false;
        for (TaskSummary ts : tasks) {
            if (ts.getProcessInstanceId() == processInstanceId) {
                completeTask(taskService, ts, user, cont);
                executed = true;
                break;
            }
        }

        if (!executed) {
            throw new RuntimeException("Task is not found. Incorrect user: "
                    + user);
        }

        System.out.println("Process started ... : processInstanceId = "
                + processInstanceId);

        return processInstanceId;
    }

    private void completeTask(final LocalTaskService taskService,
            TaskSummary ts, String user, MyContent cont) {
        em.persist(cont);
        final Long contId = cont.getId();
        final byte[] bCont = BigInteger.valueOf(contId).toByteArray();
        final ContentData cd = new ContentData();
        cd.setContent(bCont);

        taskService.start(ts.getId(), user);
        taskService.complete(ts.getId(), user, cd);
    }
}

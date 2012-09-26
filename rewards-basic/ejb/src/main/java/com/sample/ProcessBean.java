package com.sample;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.Content;
import org.jbpm.task.service.local.LocalTaskService;

import com.sample.MyKnowledgeBase.ClientSession;
import com.sample.entities.MyContent;

@Stateless
public class ProcessBean implements ProcessLocal {

    @Inject
    private MyKnowledgeBase myKnowledgeBase;

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    public long startProcess(String recipient, String content) throws Exception {
        
        MyContent cont = new MyContent();
        cont.setContentMessage(content);
        EntityManager em = emf.createEntityManager();
        em.persist(cont);

        final ClientSession session = myKnowledgeBase.createSession();
        StatefulKnowledgeSession ksession = session.getKnowledgeSession();

        // start a new process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("recipient", recipient);
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.rewards-basic", params);

        long processInstanceId = processInstance.getId();

        System.out.println("Process started ... : processInstanceId = "
                + processInstanceId);

        return processInstanceId;
    }
    
    private class MyWorkItemHandler extends SyncWSHumanTaskHandler {

        private final LocalTaskService taskClient;
        private final Long contentId;

        private MyWorkItemHandler(ClientSession session, Long contentId) {
            super(session.getTaskClient(), session.getKnowledgeSession());

            this.taskClient = session.getTaskClient();
            this.contentId = contentId;
        }

        @Override
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            super.executeWorkItem(workItem, manager);

            final BigInteger cid = BigInteger.valueOf(contentId);

            final Content content = new Content();
            content.setContent(cid.toByteArray());

            taskClient.setDocumentContent(workItem.getId(), content);
        }
    }
}

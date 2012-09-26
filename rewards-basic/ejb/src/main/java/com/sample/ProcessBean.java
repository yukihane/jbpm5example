package com.sample;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;

@Stateless
public class ProcessBean implements ProcessLocal {

    @Inject
    private MyKnowledgeBase myKnowledgeBase;

    public long startProcess(String recipient) throws Exception {

        StatefulKnowledgeSession ksession = myKnowledgeBase.createSession().getKnowledgeSession();

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
}

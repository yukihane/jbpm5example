package com.sample.workitem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.process.workitem.wsht.HumanTaskHandlerHelper;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.process.workitem.wsht.PeopleAssignmentHelper;
import org.jbpm.task.I18NText;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.SubTasksStrategy;
import org.jbpm.task.SubTasksStrategyFactory;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.kie.runtime.KnowledgeRuntime;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.WorkItem;
import org.kie.runtime.process.WorkItemManager;

public class MyWorkItemHandler extends LocalHTWorkItemHandler {

    public MyWorkItemHandler(org.jbpm.task.TaskService client, KnowledgeRuntime session) {
        super(client, session);
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        Map<String, Object> params = workItem.getParameters();
        System.out.println("params: " + params);
        Map<String, Object> results = workItem.getResults();
        System.out.println("results: " + results);
        super.executeWorkItem(workItem, manager);
    }

    @Override
    protected Task createTaskBasedOnWorkItemParams(WorkItem workItem) {
        Task task = new Task();
        String locale = (String) workItem.getParameter("Locale");
        if (locale == null) {
            locale = "en-UK";
        }
        String taskName = (String) workItem.getParameter("TaskName");
        if (taskName != null) {
            List<I18NText> names = new ArrayList<I18NText>();
            names.add(new I18NText(locale, taskName));
            task.setNames(names);
        }
        String comment = (String) workItem.getParameter("Comment");
        if (comment == null) {
            comment = "";
        }
        List<I18NText> descriptions = new ArrayList<I18NText>();
        descriptions.add(new I18NText(locale, comment));
        task.setDescriptions(descriptions);
        List<I18NText> subjects = new ArrayList<I18NText>();
        subjects.add(new I18NText(locale, comment));
        task.setSubjects(subjects);
        String priorityString = (String) workItem.getParameter("Priority");
        int priority = 0;
        if (priorityString != null) {
            try {
                priority = new Integer(priorityString);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        task.setPriority(priority);
        TaskData taskData = new TaskData();
        taskData.setWorkItemId(workItem.getId());
        taskData.setProcessInstanceId(workItem.getProcessInstanceId());
        if (session != null
                && session.getProcessInstance(workItem.getProcessInstanceId()) != null) {
            taskData.setProcessId(session
                    .getProcessInstance(workItem.getProcessInstanceId())
                    .getProcess().getId());
        }
        if (session != null && (session instanceof StatefulKnowledgeSession)) {
            taskData.setProcessSessionId(((StatefulKnowledgeSession) session)
                    .getId());
        }
        taskData.setSkipable(!"false".equals(workItem.getParameter("Skippable")));
        // Sub Task Data
        Long parentId = (Long) workItem.getParameter("ParentId");
        if (parentId != null) {
            taskData.setParentId(parentId);
        }
        String subTaskStrategiesCommaSeparated = (String) workItem
                .getParameter("SubTaskStrategies");
        if (subTaskStrategiesCommaSeparated != null
                && !subTaskStrategiesCommaSeparated.equals("")) {
            String[] subTaskStrategies = subTaskStrategiesCommaSeparated
                    .split(",");
            List<SubTasksStrategy> strategies = new ArrayList<SubTasksStrategy>();
            for (String subTaskStrategyString : subTaskStrategies) {
                SubTasksStrategy subTaskStrategy = SubTasksStrategyFactory
                        .newStrategy(subTaskStrategyString);
                strategies.add(subTaskStrategy);
            }
            task.setSubTaskStrategies(strategies);
        }

        String createdBy = (String) workItem.getParameter("CreatedBy");
        if (createdBy != null && createdBy.trim().length() > 0) {
            taskData.setCreatedBy(new User(createdBy));
        }

        task.setTaskData(taskData);

        PeopleAssignmentHelper peopleAssignmentHelper = new PeopleAssignmentHelper();
        peopleAssignmentHelper
                .handlePeopleAssignments(workItem, task, taskData);

        PeopleAssignments peopleAssignments = task.getPeopleAssignments();
        List<OrganizationalEntity> businessAdministrators = peopleAssignments
                .getBusinessAdministrators();

        task.setDeadlines(HumanTaskHandlerHelper.setDeadlines(workItem,
                businessAdministrators, session.getEnvironment()));
        return task;
    }
}

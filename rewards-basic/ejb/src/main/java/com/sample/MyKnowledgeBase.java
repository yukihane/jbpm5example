package com.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
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
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;
import org.kie.KieBase;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.SystemEventListenerFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;
import org.kie.persistence.jpa.JPAKnowledgeService;
import org.kie.runtime.Environment;
import org.kie.runtime.EnvironmentName;
import org.kie.runtime.KnowledgeRuntime;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.WorkItem;
import org.kie.runtime.process.WorkItemManager;

@Singleton
@Startup
public class MyKnowledgeBase {

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    private KnowledgeBase kbase;

    @PostConstruct
    public void init() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kbuilder.add(
                ResourceFactory.newClassPathResource("rewards-basic.bpmn2"),
                ResourceType.BPMN2);
        kbase = kbuilder.newKnowledgeBase();
    }

    private StatefulKnowledgeSession createKnowledgeSession() {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        // FIXME knowledge-apiとkie-apiそれぞれ別のorg.kie.runtime.EnvironmentNameが定義されている
//        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set("org.kie.persistence.jpa.EntityManagerFactory", emf);
        env.set("drools.persistence.jpa.EntityManagerFactory", emf);

        // FIXME 現時点でのsnapshotバージョンではKieBaseにキャストする必要がある？
        // https://issues.jboss.org/browse/DROOLS-34 で解消？
        StatefulKnowledgeSession ksession = JPAKnowledgeService
                .newStatefulKnowledgeSession((KieBase)kbase, null, env);

        new JPAWorkingMemoryDbLogger(ksession);

        return ksession;
    }

    public CommunicationPath createCommunicationPath() {
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        TaskService taskService = new TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService lts = new LocalTaskService(taskService);

        LocalHTWorkItemHandler humanTaskHandler = new MyHandler(lts, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                humanTaskHandler);

        return new CommunicationPath(lts, ksession);
    }

    public static class CommunicationPath {
        private final LocalTaskService taskService;
        private final StatefulKnowledgeSession knowledgeSession;

        private CommunicationPath(LocalTaskService taskService,
                StatefulKnowledgeSession ksession) {
            this.taskService = taskService;
            this.knowledgeSession = ksession;
        }

        public LocalTaskService getTaskService() {
            return taskService;
        }

        public StatefulKnowledgeSession getKnowledgeSession() {
            return knowledgeSession;
        }
    }

    private class MyHandler extends LocalHTWorkItemHandler {

        public MyHandler(org.jbpm.task.TaskService client,
                KnowledgeRuntime session) {
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
                    && session.getProcessInstance(workItem
                            .getProcessInstanceId()) != null) {
                taskData.setProcessId(session
                        .getProcessInstance(workItem.getProcessInstanceId())
                        .getProcess().getId());
            }
            if (session != null
                    && (session instanceof StatefulKnowledgeSession)) {
                taskData.setProcessSessionId(((StatefulKnowledgeSession) session)
                        .getId());
            }
            taskData.setSkipable(!"false".equals(workItem
                    .getParameter("Skippable")));
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
            peopleAssignmentHelper.handlePeopleAssignments(workItem, task,
                    taskData);

            PeopleAssignments peopleAssignments = task.getPeopleAssignments();
            List<OrganizationalEntity> businessAdministrators = peopleAssignments
                    .getBusinessAdministrators();

            task.setDeadlines(HumanTaskHandlerHelper.setDeadlines(workItem,
                    businessAdministrators, session.getEnvironment()));
            return task;
        }
    }
}

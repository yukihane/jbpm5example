package com.sample.workitem;

import java.util.List;

import org.jbpm.process.workitem.wsht.PeopleAssignmentHelper;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.kie.runtime.process.WorkItem;

public class MyPeopleAssignmentHelper extends PeopleAssignmentHelper {

    @Override
    protected void assignActors(WorkItem workItem,
            PeopleAssignments peopleAssignments, TaskData taskData) {

        final String actorIds;
        final String taskName = (String) workItem.getParameter("TaskName");
        if (taskName != null && taskName.trim().equals("ApprovalTask")) {
            actorIds = "mary";
        } else {
            actorIds = (String) workItem.getParameter(ACTOR_ID);
        }

        List<OrganizationalEntity> potentialOwners = peopleAssignments
                .getPotentialOwners();

        processPeopleAssignments(actorIds, potentialOwners, true);

        // Set the first user as creator ID??? hmmm might be wrong
        if (potentialOwners.size() > 0 && taskData.getCreatedBy() == null) {

            OrganizationalEntity firstPotentialOwner = potentialOwners.get(0);
            taskData.setCreatedBy((User) firstPotentialOwner);

        }

    }
}

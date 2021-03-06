/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.taskassigning.domain;

import javax.swing.Icon;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.examples.common.swingui.components.Labeled;
import org.optaplanner.examples.taskassigning.domain.solver.MovableTaskSelectionFilter;
import org.optaplanner.examples.taskassigning.domain.solver.StartTimeUpdatingVariableListener;
import org.optaplanner.examples.taskassigning.domain.solver.TaskDifficultyComparator;

@PlanningEntity(movableEntitySelectionFilter = MovableTaskSelectionFilter.class,
        difficultyComparatorClass = TaskDifficultyComparator.class)
@XStreamAlias("TaTask")
public class Task extends TaskOrEmployee implements Labeled {

    private TaskType taskType;
    private int indexInTaskType;
    private Customer customer;
    private int readyTime;
    private Priority priority;
    private boolean locked;

    // Planning variables: changes during planning, between score calculations.
    @PlanningVariable(valueRangeProviderRefs = {"employeeRange", "taskRange"},
            graphType = PlanningVariableGraphType.CHAINED)
    private TaskOrEmployee previousTaskOrEmployee;

    // Shadow variables
    // Task nextTask inherited from superclass
    @AnchorShadowVariable(sourceVariableName = "previousTaskOrEmployee")
    private Employee employee;
    @CustomShadowVariable(variableListenerClass = StartTimeUpdatingVariableListener.class,
            // Arguable, to adhere to API specs (although this works), nextTask and employee should also be a source,
            // because this shadow must be triggered after nextTask and employee (but there is no need to be triggered by those)
            sources = {@CustomShadowVariable.Source(variableName = "previousTaskOrEmployee")})
    private Integer startTime; // In minutes

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public int getIndexInTaskType() {
        return indexInTaskType;
    }

    public void setIndexInTaskType(int indexInTaskType) {
        this.indexInTaskType = indexInTaskType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(int readyTime) {
        this.readyTime = readyTime;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public TaskOrEmployee getPreviousTaskOrEmployee() {
        return previousTaskOrEmployee;
    }

    public void setPreviousTaskOrEmployee(TaskOrEmployee previousTaskOrEmployee) {
        this.previousTaskOrEmployee = previousTaskOrEmployee;
    }

    @Override
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int getMissingSkillCount() {
        if (employee == null) {
            return 0;
        }
        int count = 0;
        for (Skill skill : taskType.getRequiredSkillList()) {
            if (!employee.getSkillSet().contains(skill)) {
                count++;
            }
        }
        return count;
    }

    public int getDuration() {
        Affinity affinity = getAffinity();
        return taskType.getBaseDuration() * affinity.getDurationMultiplier();
    }

    public Affinity getAffinity() {
        return (employee == null) ? Affinity.NONE : employee.getAffinity(customer);
    }

    @Override
    public Integer getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime + getDuration();
    }

    public String getCode() {
        return taskType + "-" + indexInTaskType;
    }

    public String getTitle() {
        return taskType.getTitle();
    }

    public String getLabel() {
        return getCode() + ": " + taskType.getTitle();
    }

    public String getToolText() {
        StringBuilder toolText = new StringBuilder();
        toolText.append("<html><center><b>").append(getLabel()).append("</b><br/>")
                .append(priority.getLabel()).append("<br/><br/>");
        toolText.append("Required skills:<br/>");
        for (Skill skill : taskType.getRequiredSkillList()) {
            toolText.append(skill.getLabel()).append("<br/>");
        }
        toolText.append("<br/>Customer:<br/>").append(customer.getName()).append("<br/>(")
                .append(getAffinity().getLabel()).append(")<br/>");
        toolText.append("</center></html>");
        return toolText.toString();
    }

    @Override
    public String toString() {
        return getCode();
    }

}

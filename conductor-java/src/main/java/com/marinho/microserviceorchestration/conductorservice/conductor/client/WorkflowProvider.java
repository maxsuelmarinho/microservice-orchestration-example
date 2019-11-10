package com.marinho.microserviceorchestration.conductorservice.conductor.client;

import com.marinho.microserviceorchestration.conductorservice.domain.vo.Event;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;

public interface WorkflowProvider {

    void startWorkflow(Event event, boolean isEvent);

    Task getInProgressTask(String workflowId, String taskReferenceName);

    void updateTask(TaskResult taskResult, String taskType);

    Task getTaskDetails(String taskId);

}

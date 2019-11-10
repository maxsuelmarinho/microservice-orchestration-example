package com.marinho.microserviceorchestration.conductorservice.service;

import com.marinho.microserviceorchestration.conductorservice.domain.vo.Event;
import com.netflix.conductor.common.metadata.events.EventHandler;
import com.netflix.conductor.common.metadata.tasks.Task;
import org.springframework.http.ResponseEntity;

public interface ConductorService {

    void validateEvent(final Event event);

    void startApprovalWorkflow(final Event event, boolean isEvent);

    void validateEventHandler(final EventHandler eventHandler);

    ResponseEntity<String> createEventHandler(final EventHandler eventHandler);

    Task getTaskDetails(String taskId);
}

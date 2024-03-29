package com.marinho.microserviceorchestration.conductorservice.service;

import com.marinho.microserviceorchestration.conductorservice.conductor.client.ConductorServerProvider;
import com.marinho.microserviceorchestration.conductorservice.conductor.client.WorkflowProvider;
import com.marinho.microserviceorchestration.conductorservice.domain.vo.Event;
import com.marinho.microserviceorchestration.conductorservice.exception.ProcessorException;
import com.netflix.conductor.common.metadata.events.EventHandler;
import com.netflix.conductor.common.metadata.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("conductorService")
public class ConductorServiceImpl implements ConductorService {

    private final static Logger LOG = LoggerFactory.getLogger(ConductorServiceImpl.class);

    @Autowired
    private WorkflowProvider workflowProvider;

    @Autowired
    private ConductorServerProvider conductorServerProvider;

    @Override
    public void validateEvent(final Event event) {
        if (event.getBookingEvent() == null || event.getUserID() == null) {
            throw new ProcessorException("Event or User ID can't be null");
        }
        LOG.debug("Event validation successful");
    }

    @Override
    public void startApprovalWorkflow(final Event event, boolean isEvent) {
        workflowProvider.startWorkflow(event, isEvent);
    }

    @Override
    public void validateEventHandler(final EventHandler eventHandler) {
        if (eventHandler.getName() == null || eventHandler.getEvent() == null
                || eventHandler.getActions() == null) {
            throw new ProcessorException("EventHandler name or event or action can't be null");
        }
        LOG.debug("EventHandler validation successful");
    }

    @Override
    public ResponseEntity<String> createEventHandler(final EventHandler eventHandler) {
        return conductorServerProvider.createEventHandler(eventHandler);
    }

    @Override
    public Task getTaskDetails(String taskId) {
        return workflowProvider.getTaskDetails(taskId);
    }

}


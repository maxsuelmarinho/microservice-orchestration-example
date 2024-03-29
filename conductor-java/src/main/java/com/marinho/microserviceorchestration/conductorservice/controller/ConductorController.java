package com.marinho.microserviceorchestration.conductorservice.controller;

import com.marinho.microserviceorchestration.conductorservice.domain.vo.Event;
import com.marinho.microserviceorchestration.conductorservice.exception.ProcessorException;
import com.marinho.microserviceorchestration.conductorservice.service.ConductorService;
import com.netflix.conductor.common.metadata.events.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ConductorController {

    @Autowired
    private ConductorService conductorService;

    public static final Logger LOGGER = LoggerFactory.getLogger(ConductorController.class);

    @RequestMapping(method = RequestMethod.POST, value = "/eventhandler")
    public ResponseEntity<String> createEventHandler(
            @RequestBody(required = true) final EventHandler eventHandler) {
        LOGGER.info("Received eventHandler : [{}]", eventHandler);
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        try {
            conductorService.validateEventHandler(eventHandler);
            response = conductorService.createEventHandler(eventHandler);
        } catch (ProcessorException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/bookingprocess")
    public ResponseEntity<?> startBookingProcess(@RequestBody(required = true) final Event event,
                                                 @RequestParam(value = "isEvent", required = false) final boolean isEvent) {
        LOGGER.info("Received event : [{}]", event);
        try {
            conductorService.validateEvent(event);
            conductorService.startApprovalWorkflow(event, isEvent);
        } catch (ProcessorException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/bookingprocess/{id}")
    public ResponseEntity<?> getTaskDefs(@PathVariable("id") final String id,
                                         @RequestParam(value = "taskId", required = false) final String taskId) {
        LOGGER.info("Received [Id: {}, taskId: {}]", id, taskId);
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

}


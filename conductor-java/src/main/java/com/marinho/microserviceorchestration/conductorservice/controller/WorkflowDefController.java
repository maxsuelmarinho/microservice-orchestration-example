package com.marinho.microserviceorchestration.conductorservice.controller;

import com.marinho.microserviceorchestration.conductorservice.common.Constants;
import com.marinho.microserviceorchestration.conductorservice.conductor.client.ConductorServerProvider;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.common.metadata.workflow.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WorkflowDefController {

    @Autowired
    private MetadataClient metaDataClient;

    @Autowired
    private ConductorServerProvider conductorServerProvider;

    @Value("${server.port:8080}")
    private String port;

    public static final Logger LOGGER = LoggerFactory.getLogger(WorkflowDefController.class);

    @RequestMapping(method = RequestMethod.PUT, value = "/workflowdef")
    public void createWorkFlowDef() {
        // Sample Approval Workflow
        final WorkflowDef def = new WorkflowDef();
        def.setName(Constants.SAMPLE_WF);
        def.setVersion(1);
        def.setSchemaVersion(2);
        final List<String> wfInput = new ArrayList<>(1);
        wfInput.add(Constants.EVENT);
        def.setInputParameters(wfInput);

        final WorkflowTask simpleTask = new WorkflowTask();
        simpleTask.setName(Constants.TASK_SAMPLE);
        simpleTask.setTaskReferenceName(Constants.TASK_SAMPLE);
        simpleTask.setWorkflowTaskType(TaskType.SIMPLE);
        simpleTask.setStartDelay(0);
        Map<String, Object> input = new HashMap<>(1);
        input.put(Constants.EVENT, "${workflow.input.event}");
        simpleTask.setInputParameters(input);

        final WorkflowTask approvedDecisionTask = new WorkflowTask();
        approvedDecisionTask.setName("IS_APPROVED");
        approvedDecisionTask.setTaskReferenceName("IS_APPROVED");
        approvedDecisionTask.setWorkflowTaskType(TaskType.DECISION);
        input = new HashMap<>(4);
        input.put(Constants.APPROVED, String.format("${%s.output.approved}", Constants.TASK_SAMPLE));
        input.put("actionTakenBy", String.format("${%s.output.actionTakenBy}", Constants.TASK_SAMPLE));
        input.put("amount", String.format("${%s.output.amount}", Constants.TASK_SAMPLE));
        input.put("period", String.format("${%s.output.period}", Constants.TASK_SAMPLE));
        approvedDecisionTask.setInputParameters(input);
        approvedDecisionTask.setCaseValueParam(Constants.APPROVED);

        final List<WorkflowTask> listWFT = new ArrayList<>(1);
        final WorkflowTask httpTask = new WorkflowTask();
        httpTask.setName(Constants.TASK_HTTP);
        httpTask.setTaskReferenceName(Constants.TASK_HTTP);
        httpTask.setType(TaskType.HTTP.name());
        httpTask.setStartDelay(0);
        input = new HashMap<>(2);
        final Map<String, Object> httpRequest = new HashMap<>(3);
        httpRequest.put("method", HttpMethod.GET);

        // you can place any other REST endpoint with required payload support
        httpRequest.put("uri",
                String
                        .format("http://booking-service:%s/bookingprocess/${%s.taskId}?taskId=${CPEWF_TASK_ID}", port,
                                Constants.TASK_SAMPLE));
        httpRequest.put("contentType", "application/json");
        input.put("http_request", httpRequest);
        final Map<String, Object> body = new HashMap<>(1);
        body.put("taskId", "${CPEWF_TASK_ID}");
        input.put("body", body);
        httpTask.setInputParameters(input);

        listWFT.add(httpTask);
        final Map<String, List<WorkflowTask>> decisionCases = new HashMap<>();
        decisionCases.put("true", listWFT);
        approvedDecisionTask.setDecisionCases(decisionCases);
        approvedDecisionTask.setStartDelay(0);

        def.getTasks().add(simpleTask);
        def.getTasks().add(approvedDecisionTask);

        final Map<String, Object> output = new HashMap<>(1);
        output.put("last_task_Id", "${task_http.output..body}");
        def.setOutputParameters(output);

        // Use if you want to create NEW workflow, throws error if already
        // exist.
        // metaDataClient.registerWorkflowDef(def);

        // Sample Event Workflow
        final WorkflowDef sampleEventWorkflow = new WorkflowDef();
        sampleEventWorkflow.setName(Constants.EVENT_WF);
        sampleEventWorkflow.setVersion(1);
        sampleEventWorkflow.setSchemaVersion(2);
        final List<String> sampleEventWorkflowInput = new ArrayList<>(1);
        sampleEventWorkflowInput.add(Constants.PAYLOAD);
        sampleEventWorkflow.setInputParameters(sampleEventWorkflowInput);

        final WorkflowTask eventTask = new WorkflowTask();
        eventTask.setName(Constants.TASK_EVENT);
        eventTask.setTaskReferenceName(Constants.TASK_EVENT);
        eventTask.setSink(Constants.QUEUE_CONDUCTOR);
        eventTask.setWorkflowTaskType(TaskType.EVENT);
        eventTask.setStartDelay(0);
        Map<String, Object> eventTaskInput = new HashMap<>(1);
        // eventTaskInput.put("workflowInput",
        // "${workflow.input.workflowInput}");

        eventTaskInput.put(Constants.PAYLOAD, String.format("${workflow.input.%s}", Constants.PAYLOAD));
        eventTaskInput.put(Constants.SINK,
                String.format("%s:%s:%s", Constants.QUEUE_CONDUCTOR, Constants.EVENT_WF,
                        Constants.TASK_EVENT));
        eventTask.setInputParameters(eventTaskInput);

        sampleEventWorkflow.getTasks().add(eventTask);

        // Use if if you want to create (if not exist) / update (if exist)
        // workflow, or want to create
        // many workflows in single call.
        final List<WorkflowDef> listWF = new ArrayList<>(1);
        listWF.add(def);
        listWF.add(sampleEventWorkflow);
        metaDataClient.updateWorkflowDefs(listWF);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/workflowdef")
    public String getTaskDefs() {
        return conductorServerProvider.getWorkflowDefs().getBody();
    }

}


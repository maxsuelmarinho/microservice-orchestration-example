package main

import (
	"encoding/json"
	"fmt"
	"log"

	conductor "github.com/netflix/conductor/client/go"
	"github.com/netflix/conductor/client/go/task"
	"github.com/spf13/viper"
)

var conductorClient *conductor.ConductorHttpClient

type TaskDef struct {
	Name                   string `json:"name"`
	RetryCount             int    `json:"retryCount"`
	RetryLogic             string `json:"retryLogic"`
	RetryDelaySeconds      int    `json:"retryDelaySeconds"`
	TimeoutSeconds         int    `json:"timeoutSeconds"`
	TimeoutPolicy          string `json:"timeoutPolicy"`
	ResponseTimeoutSeconds int    `json:"responseTimeoutSeconds"`
}

type WorkflowDef struct {
	Name          string  `json:"name"`
	Description   string  `json:"description"`
	Version       int     `json:"version"`
	SchemaVersion int     `json:"schemaVersion"`
	Tasks         []*Task `json:"tasks"`
}

type Task struct {
	Name              string                 `json:"name"`
	TaskReferenceName string                 `json:"taskReferenceName"`
	InputParameters   map[string]interface{} `json:"inputParameters"`
	Type              string                 `json:"type"`
}

type DecisionTask struct {
	CaseValueParam string                 `json:"caseValueParam"`
	DecisionCases  map[string]interface{} `json:"decisionCases"`
	Task
}

func init() {
	viper.AutomaticEnv()

	conductorURL := viper.GetString("CONDUCTOR_API")
	fmt.Printf("conductor api: %s\n", conductorURL)
	conductorClient = conductor.NewConductorHttpClient(conductorURL)

	// this task will be retried 3 times on failure with 10 seconds between each retry.
	verifyIfIdentsAreAddedTaskDef := &TaskDef{
		Name:                   "verify_if_idents_are_added",
		RetryCount:             3,
		RetryLogic:             "FIXED",
		RetryDelaySeconds:      10,
		TimeoutSeconds:         300,
		TimeoutPolicy:          "TIME_OUT_WF",
		ResponseTimeoutSeconds: 180,
	}

	addIdentsTaskDef := &TaskDef{
		Name:                   "add_idents",
		RetryCount:             3,
		RetryLogic:             "FIXED",
		RetryDelaySeconds:      10,
		TimeoutSeconds:         300,
		TimeoutPolicy:          "TIME_OUT_WF",
		ResponseTimeoutSeconds: 180,
	}

	tasksDefs := []*TaskDef{verifyIfIdentsAreAddedTaskDef, addIdentsTaskDef}
	request, _ := json.Marshal(tasksDefs)

	response, err := conductorClient.RegisterTaskDefs(string(request))
	if err != nil {
		log.Fatalf("error while trying to register task definitions", err)
		return
	}
	log.Println(response)

	workflowDef := WorkflowDef{
		Name:          "add_netflix_identation",
		Description:   "Adds Netflix Identation to video files.",
		Version:       1,
		SchemaVersion: 2,
		Tasks: []*Task{
			&Task{
				Name:              "verify_if_idents_are_added",
				TaskReferenceName: "ident_verification",
				InputParameters: map[string]interface{}{
					"contentId": "${workflow.input.contentId}",
				},
				Type: "SIMPLE",
			},
			&DecisionTask{
				Name:              "decide_task",
				TaskReferenceName: "is_idents_added",
				InputParameters: map[string]interface{}{
					"case_value_param": "${ident_verification.output.is_idents_added}",
				},
				Type:           "DECISION",
				CaseValueParam: "case_value_param",
				DecisionCases: map[string]interface{}{
					"false": []*Task{
						&Task{
							Name:              "add_idents",
							TaskReferenceName: "add_idents_by_type",
							InputParameters: map[string]interface{}{
								"identType": "${workflow.input.identType}",
								"contentId": "${workflow.input.contentId}",
							},
						},
					},
				},
			},
		},
	}

	request, _ = json.Marshal(workflowDef)

	response, err = conductorClient.CreateWorkflowDef(string(request))
	if err != nil {
		log.Fatalf("error while trying to register workflow definitions", err)
		return
	}
	log.Println(response)
}

func main() {
	conductorClient.GetAllWorkflowDefs()

	conductorWorker := conductor.NewConductorWorker(viper.GetString("CONDUCTOR_API"), 1, 10000)

	conductorWorker.Start("task_1", Task_1_Execution_Function, false)
	conductorWorker.Start("task_2", Task_2_Execution_Function, true)
}

// Implementation for "task_1"
func Task_1_Execution_Function(t *task.Task) (taskResult *task.TaskResult, err error) {
	log.Println("Executing Task_1_Execution_Function for", t.TaskType)

	//Do some logic
	taskResult = task.NewTaskResult(t)

	output := map[string]interface{}{"task": "task_1", "key2": "value2", "key3": 3, "key4": false}
	taskResult.OutputData = output
	taskResult.Status = "COMPLETED"
	err = nil

	return taskResult, err
}

// Implementation for "task_2"
func Task_2_Execution_Function(t *task.Task) (taskResult *task.TaskResult, err error) {
	log.Println("Executing Task_2_Execution_Function for", t.TaskType)

	//Do some logic
	taskResult = task.NewTaskResult(t)

	output := map[string]interface{}{"task": "task_2", "key2": "value2", "key3": 3, "key4": false}
	taskResult.OutputData = output
	taskResult.Status = "COMPLETED"
	err = nil

	return taskResult, err
}

func CreateTaskDefinitions() {

}

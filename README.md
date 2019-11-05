# Microservice Orchestration Example

## Cadence

### CLI

```shell
# register a new domain named "samples-domain"
$ docker run --rm -e CADENCE_CLI_ADDRESS=cadence:7933 --network=microservice-orchestration-example_microservice-orchestration ubercadence/cli:master --domain samples-domain domain register --global_domain false

# View domain details
$ $ docker run --rm CADENCE_CLI_ADDRESS=cadence:7933 --network=microservice-orchestration-example_microservice-orchestration ubercadence/cli:master --domain samples-domain domain describe
Name: samples-domain
UUID: fcdf173f-5ac5-499e-aee1-3b4bb27e12e2
Description:
OwnerEmail:
DomainData: map[]
Status: REGISTERED
RetentionInDays: 3
EmitMetrics: false
ActiveClusterName: active
Clusters: active
HistoryArchivalStatus: ENABLED
HistoryArchivalURI: file:///tmp/cadence_archival/development
VisibilityArchivalStatus: DISABLED
Bad binaries to reset:
+-----------------+----------+------------+--------+
| BINARY CHECKSUM | OPERATOR | START TIME | REASON |
+-----------------+----------+------------+--------+
+-----------------+----------+------------+--------+

# workflow help
$ docker run --rm -e CADENCE_CLI_ADDRESS=cadence:7933 --network=microservice-orchestration-example_microservice-orchestration ubercadence/cli:master workflow run -h
# start a workflow
$ docker run --rm -e CADENCE_CLI_ADDRESS=cadence:7933 --network=microservice-orchestration-example_microservice-orchestration ubercadence/cli:master --domain samples-domain workflow run --tasklist helloWorldGroup --workflow_type main.WorkFlow --execution_time 60 --input '"cadence"'
Running execution:
  Workflow Id : f728c1a8-07c8-49d7-9fef-ea5468065b59
  Run Id      : 766094dd-7b3d-4b1a-9158-334bdcd47d97
  Type        : main.WorkFlow
  Domain      : samples-domain
  Task List   : helloWorldGroup
  Args        : "cadence"
Progress:
  1, 2019-11-05T18:59:35Z, WorkflowExecutionStarted
  2, 2019-11-05T18:59:35Z, DecisionTaskScheduled
  3, 2019-11-05T18:59:35Z, DecisionTaskStarted
  4, 2019-11-05T18:59:36Z, DecisionTaskFailed
  5, 2019-11-05T19:00:35Z, WorkflowExecutionTimedOut

Result:
  Run Time: 61 seconds
  Status: TIMEOUT
  Timeout Type: START_TO_CLOSE

# show running workers of a tasklist
$ docker run --rm -e CADENCE_CLI_ADDRESS=cadence:7933 --network=microservice-orchestration-example_microservice-orchestration ubercadence/cli:master --domain samples-domain tasklist desc --tl helloWorldGroup
DECISION POLLER IDENTITY    |   LAST ACCESS TIME
10364@TEERA@helloWorldGroup | 2019-11-05T19:04:26Z

```
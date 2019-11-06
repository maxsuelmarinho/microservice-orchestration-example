package main

import (
	"context"
	"github.com/uber-go/tally"
	"go.uber.org/cadence/.gen/go/cadence/workflowserviceclient"
	"go.uber.org/cadence/client"
	"go.uber.org/cadence/worker"
	"go.uber.org/cadence/workflow"
	"go.uber.org/yarpc"
	"go.uber.org/yarpc/transport/tchannel"
	"go.uber.org/zap"
)

// ApplicationName is the task list for this sample
const ApplicationName = "helloWorldGroup"

func init() {
	startWorkers()
}

func main() {
	startWorkflow()
}

func setupServiceConfig() {

}

func startWorkers() {
	// Initialize logger for running samples
	logger, err := zap.NewDevelopment()
	if err != nil {
		panic(err)
	}

	workerOptions := worker.Options{
		MetricsScope: tally.NoopScope,
		Logger:       logger,
	}

	domainName := "samples-domain"
	cadenceClientName := "cadence-client"
	hostPort := "127.0.0.1:7933"
	cadenceFrontendService := "cadence-frontend"

	ch, err := tchannel.NewChannelTransport(
		tchannel.ServiceName(cadenceClientName))
	if err != nil {
		logger.Fatal("Failed to create transport channel", zap.Error(err))
	}

	logger.Debug("Creating RPC dispatcher outbound",
		zap.String("ServiceName", cadenceFrontendService),
		zap.String("HostPort", hostPort))

	var dispatcher *yarpc.Dispatcher = yarpc.NewDispatcher(yarpc.Config{
		Name: cadenceClientName,
		Outbounds: yarpc.Outbounds{
			_cadenceFrontendService: {Unary: ch.NewSingleOutbound(hostPort)},
		},
	})

	if dispatcher != nil {
		if err := dispatcher.Start(); err != nil {
			logger.Fatal("Failed to create outbound transport channel: %v", zap.Error(err))
		}
	}

	var service workflowserviceclient.Interface = workflowserviceclient.New(dispatcher.ClientConfig(cadenceFrontendService))

	var clientIdentity string
	var ctxProps       []workflow.ContextPropagator
	var domainClient client.DomainClient = client.NewDomainClient(
		service, &client.Options{Identity: clientIdentity, MetricsScope: workerOptions.MetricsScope, ContextPropagators: ctxProps})
	_, err = domainClient.Describe(context.Background(), domainName)
	if err != nil {
		logger.Info("Domain doesn't exist", zap.String("Domain", domainName), zap.Error(err))
	} else {
		logger.Info("Domain successfully registered.", zap.String("Domain", domainName))
	}
	worker := worker.New(service, domainName, ApplicationName, workerOptions)
	err = worker.Start()
	if err != nil {
		logger.Error("Failed to start workers.", zap.Error(err))
		panic("Failed to start workers")
	}
}

func startWorkflow() {

}

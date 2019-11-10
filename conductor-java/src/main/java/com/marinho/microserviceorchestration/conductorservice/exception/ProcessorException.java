package com.marinho.microserviceorchestration.conductorservice.exception;

public class ProcessorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProcessorException(final String string) {
        super(string);
    }

}

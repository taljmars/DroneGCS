package com.dronegcs.console.operations;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.dronegcs.console.services.LoggerDisplayerSvc;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class OperationHandler
{
	@Autowired @NotNull(message = "Internal Error: Failed to get log displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	protected OperationHandler next;
	
	public void setNext(OperationHandler next)
	{
		this.next = next;
	}

	public boolean go() throws InterruptedException {
		if (next != null)
			return next.go();
		
		loggerDisplayerSvc.logGeneral("Last Phase done");
		return true;
	}
}

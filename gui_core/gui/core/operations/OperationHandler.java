package gui.core.operations;

public abstract class OperationHandler
{
	protected OperationHandler next;
	
	public void setNext(OperationHandler next)
	{
		this.next = next;
	}

	public boolean go() throws InterruptedException {
		if (next != null)
			return next.go();
		
		return true;
	}
}

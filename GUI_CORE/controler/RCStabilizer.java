package controler;

import logger.Logger;

public class RCStabilizer implements Runnable {
	
	public RCStabilizer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		Logger.LogGeneralMessege(this.getClass().getName() + " Thread started");
		while (true) {
			try {
				//Thread.sleep(1000);
				Thread.sleep(100);
				KeyBoardControl.get().Update();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

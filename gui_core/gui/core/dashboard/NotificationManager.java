package gui.core.dashboard;

import gui.is.NotificationsHandler;

import java.util.TimerTask;
import java.util.Vector;

public class NotificationManager extends TimerTask {

	protected static final long MSG_CHECK_PERIOD = 1000;

	private Vector<String> messegeQueue = null;
	private NotificationsHandler notificationHandler = null;

	public NotificationManager(NotificationsHandler handler) {
		messegeQueue = new Vector<String>();
		notificationHandler = handler;
	}

	private synchronized String pop() {
		if (messegeQueue.isEmpty())
			return null;

		String msg = messegeQueue.get(0);
		messegeQueue.remove(0);
		return msg;
	}

	public synchronized void add(String string) {
		messegeQueue.addElement(string);
	}

	public synchronized boolean isEmpty() {
		return messegeQueue.isEmpty();
	}

	@Override
	public void run() {
		if (isEmpty()) {
			notificationHandler.ClearNotification();
			return;
		}

		String msg = pop();
		notificationHandler.SetNotification("   " + msg + "   ");
	}
}

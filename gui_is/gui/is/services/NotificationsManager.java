package gui.is.services;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class NotificationsManager {

	protected static final long MSG_CHECK_PERIOD = 1000;

	private static NotificationsManager instance = null;
	private Vector<String> messegeQueue = null;
	private Set<NotificationsListener> notificationListeners = null;
	private Timer timer = null;
	
	private static Object lock =  new Object();

	private NotificationsManager() {
		messegeQueue = new Vector<String>();
		notificationListeners = new HashSet<NotificationsListener>();
		timer = new Timer();
	}
	
	private void start() {
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				if (isEmpty()) {
					for (NotificationsListener nl : notificationListeners)
						nl.ClearNotification();
					return;
				}

				String msg = pop();
				for (NotificationsListener nl : notificationListeners)
					nl.SetNotification("   " + msg + "   ");
			};
		};
		timer.scheduleAtFixedRate(tt, 0, NotificationsManager.MSG_CHECK_PERIOD);
	}
	
	public static NotificationsManager LazyInit() {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new NotificationsManager();
					instance.start();
				}
			}
		}
		
		return instance;
	}
	
	// TODO: Verify it was initialized
	public static void addNotificationListener(NotificationsListener listener) {
		LazyInit();
		instance.notificationListeners.add(listener);
	}
	
	public static void removeNotificationListener(NotificationsListener listener) {
		LazyInit();
		instance.notificationListeners.remove(listener);
	}

	private synchronized String pop() {
		if (messegeQueue.isEmpty())
			return null;

		String msg = messegeQueue.get(0);
		messegeQueue.remove(0);
		return msg;
	}

	public static synchronized void add(String string) {
		LazyInit();
		instance.messegeQueue.addElement(string);
	}

	private synchronized boolean isEmpty() {
		return messegeQueue.isEmpty();
	}
}

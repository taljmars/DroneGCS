package com.objects_detector.trackers;

import com.objects_detector.trackers.ColorTracker.ColorTracker;
import com.objects_detector.trackers.ColorTrackerLockSingleObject.ColorTrackerLockSingleObject;
import com.objects_detector.trackers.FakeTracker.FakeTracker;
import com.objects_detector.trackers.MovementTracker.MovmentTracker;

public enum TrackersEnum {
	
	MOVEMENT_TRACKER(MovmentTracker.name),
	COLOR_TRACKER(ColorTracker.name),
	COLOR_TRACKER_SINGLE_OBJECT(ColorTrackerLockSingleObject.name),
	VIDEO_ONLY(FakeTracker.name);

	TrackersEnum(String name) {
		this.name = name;
	}
	
	public String name;
	
	public String toString() {
		return name;
	}
}

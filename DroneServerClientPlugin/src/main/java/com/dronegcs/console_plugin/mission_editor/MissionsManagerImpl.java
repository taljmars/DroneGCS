package com.dronegcs.console_plugin.mission_editor;

import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.QueryRequestRemote;
import com.db.persistence.scheme.QueryResponseRemote;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.MissionItem;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.QuerySvcRemoteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.*;

@Component
public class MissionsManagerImpl implements MissionsManager {

	private Map<String, BaseObject> dbItems;
	private Map<String, BaseObject> dirtyDeleted;
	private Map<String, BaseObject> dirtyItems;

	private final static Logger LOGGER = LoggerFactory.getLogger(MissionsManagerImpl.class);

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	private ApplicationContext applicationContext;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
	private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get query")
	private QuerySvcRemoteWrapper querySvcRemote;

	public MissionsManagerImpl() {
		dbItems = new HashMap<>();
		dirtyDeleted = new HashMap<>();
		dirtyItems = new HashMap<>();
	}

	@Override
	public MissionEditor openMissionEditor(String initialName) {
		LOGGER.debug("Open editor using mission editor");
		if (initialName == null || initialName.isEmpty()) {
			throw new RuntimeException("Mission name cannot be empty");
		}
		ClosableMissionEditor missionEditor = applicationContext.getBean(ClosableMissionEditor.class);
		missionEditor.open(initialName);
		return missionEditor;
	}

	@Override
	public MissionEditor openMissionEditor(Mission mission) {
		assert mission != null;
		LOGGER.debug(String.format("Setting new mission to mission editor '%s'", mission));
		ClosableMissionEditor missionEditor = applicationContext.getBean(ClosableMissionEditor.class);
		missionEditor.open(mission);
		return missionEditor;
	}

	@Override
	public Mission getMission(String missionUid) {
		Mission res = (Mission) dirtyItems.get(missionUid);
		if (res == null)
			res = (Mission) dbItems.get(missionUid);
		return res;
	}

	@Override
	public MissionItem getMissionItem(String missionItemUid) {
		MissionItem res = (MissionItem) dirtyItems.get(missionItemUid);
		if (res == null)
			res = (MissionItem) dbItems.get(missionItemUid);
		return res;
	}

	@Override
	public List<MissionItem> getMissionItems(Mission mission) {
		List<MissionItem> missionItemList = new ArrayList<>();

		List<String> uuidList = mission.getMissionItemsUids();
		uuidList.forEach((String uuid) -> {
			MissionItem mItem = getMissionItem(uuid);
			missionItemList.add(mItem);
		});

		return missionItemList;
	}

    @Override
    public Collection<ClosingPair<BaseObject>> flushAllItems(boolean isPublish) {
	    List<ClosingPair<BaseObject>> res = new ArrayList<>();
	    try {
            for (BaseObject a : this.dirtyItems.values()) {
				BaseObject updatedObj = objectCrudSvcRemote.update(a);
				if (updatedObj instanceof  Mission) {
					LOGGER.debug("Adding to Publish-update list: {}", updatedObj);
                	res.add(new ClosingPair<>((Mission) updatedObj, false));
				}
                dbItems.put(updatedObj.getKeyId().getObjId(), updatedObj);
            }
            this.dirtyItems.clear();
            for (BaseObject deletedObj : this.dirtyDeleted.values()) {
                objectCrudSvcRemote.delete(deletedObj);
				if (deletedObj instanceof  Mission) {
					LOGGER.debug("Adding to Publish-delete list: {}", deletedObj);
					res.add(new ClosingPair<>((Mission) deletedObj, false));
				}
                dbItems.remove(deletedObj.getKeyId().getObjId());
            }
            this.dirtyDeleted.clear();
        }
        catch (Exception e ) {
            System.out.println(e.getMessage());
        }
        return res;
    }

	@Override
	public boolean isDirty(BaseObject item) {
		String key = item.getKeyId().getObjId();
		return dirtyItems.containsKey(key) || dirtyDeleted.containsKey(key);
	}

	@Override
	public void load(BaseObject item) {
		dbItems.put(item.getKeyId().getObjId(), item);
	}

	@Override
	public List<BaseObject> getAllMissions() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Mission.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllMissions");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> missionList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} missions in total", missionList.size());
		for (BaseObject a : missionList) {
			dbItems.put(a.getKeyId().getObjId(), a);
		}
		return missionList;
	}

	@Override
	public List<BaseObject> getAllModifiedMissions() {
		List<BaseObject> missionList = new ArrayList<>();
		for (BaseObject a : dirtyItems.values()) {
			if (a instanceof Mission)
				missionList.add(a);
		}
		return missionList;
	}

	@Override
	public void updateItem(BaseObject object) {
		dirtyItems.put(object.getKeyId().getObjId(), object);
	}

	@Override
	public void removeItem(BaseObject object) {
		String key = object.getKeyId().getObjId();
		dirtyItems.remove(key);
		dbItems.remove(key);
		dirtyDeleted.put(key, object);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Missions Manager Status:\n");
//		for (ClosableMissionEditor closableMissionEditor : closableMissionEditorList.values()) {
//			builder.append(closableMissionEditor);
//		}
		return builder.toString();
	}
}

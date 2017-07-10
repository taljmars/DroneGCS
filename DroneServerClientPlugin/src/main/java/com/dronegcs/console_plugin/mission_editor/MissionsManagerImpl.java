package com.dronegcs.console_plugin.mission_editor;

import com.dronedb.persistence.scheme.*;
import com.dronedb.persistence.ws.internal.DatabaseValidationRemoteException;
import com.dronedb.persistence.ws.internal.MissionCrudSvcRemote;
import com.dronedb.persistence.ws.internal.QuerySvcRemote;
import com.dronedb.persistence.ws.internal.DroneDbCrudSvcRemote;
import com.dronedb.persistence.ws.internal.ObjectNotFoundException;
import com.dronedb.persistence.ws.internal.ObjectNotFoundRemoteException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.*;

@Component
public class MissionsManagerImpl implements MissionsManager {

	private final static Logger logger = Logger.getLogger(MissionsManagerImpl.class);

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	private ApplicationContext applicationContext;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
	private DroneDbCrudSvcRemote droneDbCrudSvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get query")
	private QuerySvcRemote querySvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get mission object crud")
	private MissionCrudSvcRemote missionCrudSvcRemote;

	private List<ClosableMissionEditor> closableMissionEditorList;

	public MissionsManagerImpl() {
		closableMissionEditorList = new ArrayList<>();
	}

	@PostConstruct
	public void init() {
		List<BaseObject> missionList = getAllModifiedMissions();
		for (BaseObject item : missionList) {
			Mission mission = (Mission) item;
			try {
				logger.debug("Mission '" + mission.getName() + "' is in edit mode");
				openMissionEditor(mission);
			} catch (MissionUpdateException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public MissionEditor openMissionEditor(String initialName) throws MissionUpdateException {
		logger.debug("Setting new mission to mission editor");
		ClosableMissionEditor missionEditor = applicationContext.getBean(ClosableMissionEditor.class);
		missionEditor.open(initialName);
		closableMissionEditorList.add(missionEditor);
		return missionEditor;
	}

	@Override
	public void delete(Mission mission) {
		if (mission == null) {
			logger.error("Received Empty mission, skip deletion");
			return;
		}

		try {
			ClosableMissionEditor closableMissionEditor = (ClosableMissionEditor) openMissionEditor(mission);
			closableMissionEditor.delete();
		}
		catch (MissionUpdateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Mission update(Mission mission) throws MissionUpdateException {
		try {
			ClosableMissionEditor closableMissionEditor = findMissionEditorByMission(mission);
			if (closableMissionEditor == null)
				return (Mission) droneDbCrudSvcRemote.update(mission);

			return closableMissionEditor.update(mission);
		}
		catch (DatabaseValidationRemoteException e) {
			throw new MissionUpdateException(e.getMessage());
		}
	}

	@Override
	public List<MissionItem> getMissionItems(Mission mission) {
		Mission leadMission = mission;
		ClosableMissionEditor closableMissionEditor = findMissionEditorByMission(mission);
		if (closableMissionEditor != null)
			leadMission = closableMissionEditor.getModifiedMission();

		List<MissionItem> missionItemList = new ArrayList<>();
		List<String> uuidList = leadMission.getMissionItemsUids();
		for (String uuid : uuidList) {
			try {
				missionItemList.add((MissionItem) droneDbCrudSvcRemote.readByClass(uuid.toString(), MissionItem.class.getName()));
			} catch (ObjectNotFoundException e) {
				e.printStackTrace();
				// TODO
			}
		}

		return missionItemList;
	}

	@Override
	public Mission cloneMission(Mission mission) throws MissionUpdateException {
		try {
			return missionCrudSvcRemote.cloneMission(mission);
		}
		catch (DatabaseValidationRemoteException e) {
			throw new MissionUpdateException(e.getMessage());
		} catch (ObjectNotFoundRemoteException e) {
			throw new MissionUpdateException(e.getMessage());
		}
	}

	@Override
	public Collection<MissionClosingPair> closeAllMissionEditors(boolean shouldSave) {
		Collection<MissionClosingPair> closedMissions = new ArrayList<>();
		Iterator<ClosableMissionEditor> it = closableMissionEditorList.iterator();
		while (it.hasNext()) {
			closedMissions.add(it.next().close(shouldSave));
		}
		closableMissionEditorList.clear();
		return closedMissions;
	}

	@Override
	public MissionEditor openMissionEditor(Mission mission) throws MissionUpdateException {
		logger.debug("Setting new mission to mission editor");
		ClosableMissionEditor missionEditor = findMissionEditorByMission(mission);
		if (missionEditor == null) {
			System.err.println("Editor not exist for mission " + mission.getName() + ", creating new one");
			missionEditor = applicationContext.getBean(ClosableMissionEditor.class);
			missionEditor.open(mission);
			closableMissionEditorList.add(missionEditor);
		}
		else {
			System.err.println("Found existing mission editor");
		}
		return missionEditor;
	}

	@Override
	public <T extends MissionEditor> T getMissionEditor(Mission mission) {
		return (T) findMissionEditorByMission(mission);
	}

	@Override
	public <T extends MissionEditor> MissionClosingPair closeMissionEditor(T missionEditor, boolean shouldSave) {
		logger.debug("closing mission editor");
		if (!(missionEditor instanceof ClosableMissionEditor)) {
			return null;
		}
		MissionClosingPair missionClosingPair = ((ClosableMissionEditor) missionEditor).close(shouldSave);
		closableMissionEditorList.remove(missionEditor);
		return missionClosingPair;
	}

	@Override
	public List<BaseObject> getAllMissions() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Mission.class.getName());
		queryRequestRemote.setQuery("GetAllMissions");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> missionList = queryResponseRemote.getResultList();
		return missionList;
	}

	@Override
	public List<BaseObject> getAllModifiedMissions() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Mission.class.getName());
		queryRequestRemote.setQuery("GetAllModifiedMissions");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> missionList = queryResponseRemote.getResultList();
		return missionList;
	}

	private ClosableMissionEditor findMissionEditorByMission(Mission mission) {
		for (ClosableMissionEditor closableMissionEditor : closableMissionEditorList) {
			if (mission.equals(closableMissionEditor.getModifiedMission())) {
				return closableMissionEditor;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Missions Manager Status:\n");
		for (ClosableMissionEditor closableMissionEditor : closableMissionEditorList) {
			builder.append(closableMissionEditor);
		}
		return builder.toString();
	}
}

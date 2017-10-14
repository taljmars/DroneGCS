package com.dronegcs.console_plugin.mission_editor;

import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.db.persistence.wsSoap.QueryRequestRemote;
import com.db.persistence.wsSoap.QueryResponseRemote;
import com.dronedb.persistence.scheme.*;
import com.dronedb.persistence.ws.*;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.MissionCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.QuerySvcRemoteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.*;

@Component
public class MissionsManagerImpl implements MissionsManager {

	private final static Logger LOGGER = LoggerFactory.getLogger(MissionsManagerImpl.class);

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	private ApplicationContext applicationContext;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
	private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get query")
	private QuerySvcRemoteWrapper querySvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get mission object crud")
	private MissionCrudSvcRemoteWrapper missionCrudSvcRemote;

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
				LOGGER.debug("Mission '" + mission.getName() + "' is in edit mode");
				openMissionEditor(mission);
			} catch (MissionUpdateException e) {
				LOGGER.error("Failed to initialize mission manager", e);
			}
		}
	}

	@Override
	public MissionEditor openMissionEditor(String initialName) throws MissionUpdateException {
		LOGGER.debug("Setting new mission to mission editor");
		ClosableMissionEditor missionEditor = applicationContext.getBean(ClosableMissionEditor.class);
		missionEditor.open(initialName);
		closableMissionEditorList.add(missionEditor);
		return missionEditor;
	}

	@Override
	public MissionEditor openMissionEditor(Mission mission) throws MissionUpdateException {
		LOGGER.debug("Setting new mission to mission editor");
		ClosableMissionEditor missionEditor = findMissionEditorByMission(mission);
		if (missionEditor == null) {
			LOGGER.debug("Editor not exist for mission " + mission.getName() + ", creating new one");
			missionEditor = applicationContext.getBean(ClosableMissionEditor.class);
			missionEditor.open(mission);
			closableMissionEditorList.add(missionEditor);
		}
		else {
			LOGGER.debug("Found existing mission editor");
		}
		return missionEditor;
	}

	@Override
	public void delete(Mission mission) {
		if (mission == null) {
			LOGGER.error("Received Empty mission, skip deletion");
			return;
		}

		try {
			ClosableMissionEditor closableMissionEditor = (ClosableMissionEditor) openMissionEditor(mission);
			closableMissionEditor.delete();
		}
		catch (MissionUpdateException e) {
			LOGGER.error("Failed to delete mission", e);
		}
	}

	@Override
	public Mission update(Mission mission) throws MissionUpdateException {
		try {
			ClosableMissionEditor closableMissionEditor = findMissionEditorByMission(mission);
			if (closableMissionEditor == null)
				return (Mission) objectCrudSvcRemote.update(mission);

			return closableMissionEditor.update(mission);
		}
		catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException  e) {
			throw new MissionUpdateException(e.getMessage());
		}
	}

	@Override
	public Mission cloneMission(Mission mission) throws MissionUpdateException {
		try {
			return missionCrudSvcRemote.cloneMission(mission);
		}
		catch (ObjectNotFoundRemoteException | DatabaseValidationRemoteException | ObjectInstanceRemoteException  e) {
			throw new MissionUpdateException(e.getMessage());
		}
	}

	@Override
	public <T extends MissionEditor> ClosingPair closeMissionEditor(T missionEditor, boolean shouldSave) {
		LOGGER.debug("closing mission editor");
		if (!(missionEditor instanceof ClosableMissionEditor)) {
			return null;
		}
		ClosingPair<Mission> missionClosingPair = ((ClosableMissionEditor) missionEditor).close(shouldSave);
		closableMissionEditorList.remove(missionEditor);
		return missionClosingPair;
	}

	@Override
	public Collection<ClosingPair<Mission>> closeAllMissionEditors(boolean shouldSave) {
		Collection<ClosingPair<Mission>> closedMissions = new ArrayList<>();
		Iterator<ClosableMissionEditor> it = closableMissionEditorList.iterator();
		while (it.hasNext()) {
			closedMissions.add(it.next().close(shouldSave));
		}
		closableMissionEditorList.clear();
		return closedMissions;
	}

	@Override
	public <T extends MissionEditor> T getMissionEditor(Mission mission) {
		return (T) findMissionEditorByMission(mission);
	}

	@Override
	public List<MissionItem> getMissionItems(Mission mission) {
		Mission leadMission = mission;
		ClosableMissionEditor closableMissionEditor = findMissionEditorByMission(mission);
		if (closableMissionEditor != null)
			leadMission = closableMissionEditor.getModifiedMission();

		List<MissionItem> missionItemList = new ArrayList<>();
		List<UUID> uuidList = leadMission.getMissionItemsUids();
		for (UUID uuid : uuidList) {
			try {
				missionItemList.add((MissionItem) objectCrudSvcRemote.readByClass(uuid, MissionItem.class.getCanonicalName()));
			}
			catch (ObjectNotFoundRemoteException e) {
				LOGGER.error("Failed to get mission item", e);
			}
		}

		return missionItemList;
	}

	@Override
	public List<BaseObject> getAllMissions() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Mission.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllMissions");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> missionList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} missions in total", missionList.size());
		return missionList;
	}

	@Override
	public List<BaseObject> getAllModifiedMissions() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Mission.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllModifiedMissions");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> missionList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} modified missions in total", missionList.size());
		return missionList;
	}

	private ClosableMissionEditor findMissionEditorByMission(Mission mission) {
		for (ClosableMissionEditor closableMissionEditor : closableMissionEditorList) {
			Mission cloMission = closableMissionEditor.getModifiedMission();
			if (mission.getKeyId().getObjId().equals(cloMission.getKeyId().getObjId()))
				return closableMissionEditor;

			if (mission.equals(cloMission)) {
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

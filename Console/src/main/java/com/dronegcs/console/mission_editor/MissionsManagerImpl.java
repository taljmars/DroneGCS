package com.dronegcs.console.mission_editor;

import javax.validation.constraints.NotNull;

import com.dronedb.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.apis.*;
import com.dronedb.persistence.scheme.mission.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.dronegcs.console.services.DialogManagerSvc;
import com.dronegcs.console.services.LoggerDisplayerSvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MissionsManagerImpl implements MissionsManager {

	@Autowired @NotNull(message = "Internal Error: Failed to get log displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	private ApplicationContext applicationContext;

	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;

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

	@Override
	public MissionEditor openMissionEditor(String initialName) {
		loggerDisplayerSvc.logGeneral("Setting new mission to mission editor");
		ClosableMissionEditor missionEditor = applicationContext.getBean(ClosableMissionEditor.class);
		missionEditor.open(initialName);
		closableMissionEditorList.add(missionEditor);
		return missionEditor;
	}

	@Override
	public void delete(Mission mission) {
		Mission oldMission = null;
		ClosableMissionEditor closableMissionEditor = findMissionEditorByMission(mission);
		if (closableMissionEditor == null)
			return;

		oldMission = closableMissionEditor.close(false);
		if (oldMission != null) {
			droneDbCrudSvcRemote.delete(oldMission);
		}
	}

	@Override
	public Mission update(Mission mission) {
		ClosableMissionEditor closableMissionEditor = findMissionEditorByMission(mission);
		if (closableMissionEditor == null)
			return droneDbCrudSvcRemote.update(mission);

		return closableMissionEditor.update(mission);
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
			missionItemList.add(droneDbCrudSvcRemote.readByClass(uuid, MissionItem.class));
		}

		return missionItemList;
	}

	@Override
	public MissionEditor openMissionEditor(Mission mission) {
		loggerDisplayerSvc.logGeneral("Setting new mission to mission editor");
		ClosableMissionEditor missionEditor = applicationContext.getBean(ClosableMissionEditor.class);
		missionEditor.open(mission);
		closableMissionEditorList.add(missionEditor);
		return missionEditor;
	}

	@Override
	public <T extends MissionEditor> T getMissionEditor(Mission mission) {
		return (T) findMissionEditorByMission(mission);
	}

	@Override
	public <T extends MissionEditor> Mission closeMissionEditor(T missionEditor, boolean shouldSave) {
		loggerDisplayerSvc.logGeneral("closing mission editor");
		if (!(missionEditor instanceof ClosableMissionEditor)) {
			return null;
		}
		Mission mission = ((ClosableMissionEditor) missionEditor).close(shouldSave);
		closableMissionEditorList.remove(missionEditor);
		return mission;
	}

	@Override
	public List<BaseObject> getAllMissions() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Mission.class);
		queryRequestRemote.setQuery("GetAllMissions");
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
}

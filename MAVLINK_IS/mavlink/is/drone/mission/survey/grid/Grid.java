package mavlink.is.drone.mission.survey.grid;

import java.util.List;

import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.geoTools.PolylineTools;
import mavlink.is.utils.units.Length;

public class Grid {
	public List<Coord2D> gridPoints;
	private List<Coord2D> cameraLocations;

	public Grid(List<Coord2D> list, List<Coord2D> cameraLocations) {
		this.gridPoints = list;
		this.cameraLocations = cameraLocations;
	}

	public Length getLength() {
		return PolylineTools.getPolylineLength(gridPoints);
	}

	public int getNumberOfLines() {
		return gridPoints.size() / 2;
	}

	public List<Coord2D> getCameraLocations() {
		return cameraLocations;
	}

	public int getCameraCount() {
		return getCameraLocations().size();
	}

}
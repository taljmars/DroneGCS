package mavlink.is.drone.mission.survey.grid;

import java.util.ArrayList;
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

	public Grid(Grid grid) {		
		if (grid.gridPoints != null) {
			gridPoints = new ArrayList<>();
			for (Coord2D coord2d : grid.gridPoints) gridPoints.add(new Coord2D(coord2d));
		}
		
		if (grid.cameraLocations != null) {
			cameraLocations = new ArrayList<>();
			for (Coord2D coord2d : grid.cameraLocations) cameraLocations.add(new Coord2D(coord2d));
		}
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
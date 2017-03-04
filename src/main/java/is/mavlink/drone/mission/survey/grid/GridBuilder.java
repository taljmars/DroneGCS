package is.mavlink.drone.mission.survey.grid;

import java.util.List;

import gui.is.shapes.LineCoordinates;
import gui.is.shapes.Polygon;
import is.mavlink.drone.mission.survey.SurveyData;
import tools.geoTools.Coordinate;

public class GridBuilder {

	private Polygon poly;
	private Double angle;
	private Double lineDist;
	private Coordinate origin;
	private Double wpDistance;

	private Grid grid;

	public GridBuilder(Polygon polygon, SurveyData surveyData, Coordinate originPoint) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = surveyData.getAngle();
		this.lineDist = surveyData.getLateralPictureDistance();
		this.wpDistance = surveyData.getLongitudinalPictureDistance();
	}

	public GridBuilder(Polygon polygon, double angle, double distance, Coordinate originPoint) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = angle;
		this.lineDist = distance;
		this.wpDistance = distance;
	}
	
	public void setAngle(double newAngle){
		angle = newAngle;
	}

	public Grid generate(boolean sort) throws Exception {
		List<Coordinate> polygonPoints = poly.getPoints();

		List<LineCoordinates> circumscribedGrid = new CircumscribedGrid(polygonPoints, angle, lineDist).getGrid();
		List<LineCoordinates> trimedGrid = new Trimmer(circumscribedGrid, poly.getLines()).getTrimmedGrid();
		EndpointSorter gridSorter = new EndpointSorter(trimedGrid, wpDistance);
		gridSorter.sortGrid(origin, sort);
		grid = new Grid(gridSorter.getSortedGrid(), gridSorter.getCameraLocations());
		return grid;
	}

}

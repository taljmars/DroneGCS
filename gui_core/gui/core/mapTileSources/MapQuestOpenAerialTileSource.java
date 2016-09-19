// License: GPL. For details, see Readme.txt file.
package gui.core.mapTileSources;

import gui.is.interfaces.ICoordinate;

public class MapQuestOpenAerialTileSource extends AbstractMapQuestTileSource {

    //private static final String PATTERN = "http://oatile%d.mqcdn.com/tiles/1.0.0/sat";
	private static final String PATTERN = "http://otile%d.mqcdn.com/tiles/1.0.0/sat";

    public MapQuestOpenAerialTileSource() {
        super("MapQuest Open Aerial", PATTERN, "mapquest-oa");
    }

    @Override
    public String getAttributionText(int zoom, ICoordinate topLeft, ICoordinate botRight) {
        return "Portions Courtesy NASA/JPL-Caltech and U.S. Depart. of Agriculture, Farm Service Agency - "+MAPQUEST_ATTRIBUTION;
    }

    @Override
    public String getAttributionLinkURL() {
        return MAPQUEST_WEBSITE;
    }
}
package gui.core.mapTileSources;

import gui.is.mapTileSources.AbstractOsmTileSource;

public class OsmCartoMapTileSource extends AbstractOsmTileSource {

    private static final String PATTERN = "http://%s.tile.openstreetmap.org";
    
    private static final String[] SERVER = {"a", "b", "c"};
    
    private int serverNum;

    public OsmCartoMapTileSource() {
        super("Standard Carto OSM", PATTERN, "Carto OSM");
    }

    @Override
    public String getBaseUrl() {
        String url = String.format(this.baseUrl, new Object[] {SERVER[serverNum]});
        serverNum = (serverNum + 1) % SERVER.length;
        return url;
    }

    @Override
    public int getMaxZoom() {
        return 18;
    }
}
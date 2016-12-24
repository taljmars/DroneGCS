package gui.core.mapTileSources;

import gui.is.mapTileSources.AbstractOsmTileSource;

public class OsmHotMapTileSource extends AbstractOsmTileSource {

    private static final String PATTERN = "http://%s.tile.openstreetmap.fr/hot";

    private static final String[] SERVER = {"a", "b", "c"};

    private int serverNum;

    public OsmHotMapTileSource() {
        super("Hot", PATTERN, "Humanitarian focused OSM");
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
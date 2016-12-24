package gui.core.mapTileSources;

import gui.is.mapTileSources.AbstractOsmTileSource;

public class OsmDarkMapTileSource extends AbstractOsmTileSource {

    private static final String PATTERN = "http://%s.basemaps.cartocdn.com/dark_all";

    private static final String[] SERVER = {"a", "b", "c"};

    private int serverNum;

    public OsmDarkMapTileSource() {
        super("DarkMap", PATTERN, "Dark Carto OSM");
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
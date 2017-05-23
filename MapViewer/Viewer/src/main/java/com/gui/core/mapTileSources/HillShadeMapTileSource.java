package com.gui.core.mapTileSources;

import com.gui.is.mapTileSources.AbstractOsmTileSource;

public class HillShadeMapTileSource extends AbstractOsmTileSource {
  
    private static final String PATTERN = "http://%s.tiles.wmflabs.org/hillshading";

    private static final String[] SERVER = {"a", "b", "c"};

    private int serverNum;

    public HillShadeMapTileSource() {
        super("HillShading", PATTERN, "HillShading");
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
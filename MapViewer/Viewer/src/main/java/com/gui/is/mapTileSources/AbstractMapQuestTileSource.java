// License: GPL. For details, see Readme.txt file.
package com.gui.is.mapTileSources;

public class AbstractMapQuestTileSource extends AbstractOsmTileSource {

    private static final int NUMBER_OF_SERVERS = 4;

    private int SERVER_NUM = 1;

    public AbstractMapQuestTileSource(String name, String baseUrl, String id) {
        super(name, baseUrl, id);
    }

    @Override
    public String getBaseUrl() {
        String url = String.format(this.baseUrl, SERVER_NUM);
        SERVER_NUM = (SERVER_NUM % NUMBER_OF_SERVERS) + 1;
        return url;
    }
}

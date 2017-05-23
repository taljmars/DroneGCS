// License: GPL. For details, see Readme.txt file.
package com.gui.is.mapTileSources;

/**
 * Abstract class for OSM Tile sources
 */
public abstract class AbstractOsmTileSource extends TMSTileSource {

    /**
     * Constructs a new OSM tile source
     * @param name Source name as displayed in GUI
     * @param baseUrl Source URL
     * @param id unique id for the tile source; contains only characters that
     * are safe for file names; can be null
     */
    public AbstractOsmTileSource(String name, String baseUrl, String id) {
        super(new TileSourceInfo(name, baseUrl, id));
    }

    @Override
    public int getMaxZoom() {
        return 19;
    }
}

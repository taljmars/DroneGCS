// License: GPL. For details, see Readme.txt file.
package com.gui.is.interfaces.maptiles;

import com.gui.core.mapTileControl.MapTile;

/**
 * Implement this interface for creating your custom tile cache for
 * {@link JMapViewer}.
 *
 * @author Jan Peter Stotz
 */
public interface MapTileCache {

    /**
     * Retrieves a tile from the cache if present, otherwise <code>null</code>
     * will be returned.
     *
     * @param source
     *            the tile source
     * @param x
     *            tile number on the x axis of the tile to be retrieved
     * @param y
     *            tile number on the y axis of the tile to be retrieved
     * @param z
     *            zoom level of the tile to be retrieved
     * @return the requested tile or <code>null</code> if the tile is not
     *         present in the cache
     */
	MapTile getTile(TileSource source, int x, int y, int z);

    /**
     * Adds a tile to the cache. How long after adding a tile can be retrieved
     * via {@link #getTile(TileSource, int, int, int)} is unspecified and depends on the
     * implementation.
     *
     * @param tile the tile to be added
     */
    void addTile(MapTile tile);

    /**
     * @return the number of tiles hold by the cache
     */
    int getTileCount();

    /**
     * Clears the cache deleting all tiles from memory.
     */
    void clear();

    /**
     * Size of the cache.
     * @return maximum number of tiles in cache
     */
    int getCacheSize();
}

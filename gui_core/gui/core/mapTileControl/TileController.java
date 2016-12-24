package gui.core.mapTileControl;

import gui.is.interfaces.maptiles.MapTileCache;
import gui.is.interfaces.maptiles.TileSource;

public class TileController {
	
    protected MapTileCache tileCache;
    protected TileSource tileSource;
    
    public TileController(TileSource source) {
		this(source, new MemoryMapTileCache());
	}

    private TileController(TileSource source, MapTileCache tileCache) {
        this.tileSource = source;
        this.tileCache = tileCache;
    }

	/**
     * retrieves a tile from the cache. If the tile is not present in the cache
     * a load job is added to the working queue
     *
     * @param tilex the X position of the tile
     * @param tiley the Y position of the tile
     * @param zoom the zoom level of the tile
     * @return specified tile from the cache or <code>null</code> if the tile
     *         was not found in the cache.
     */
    public MapTile getTile(int tilex, int tiley, int zoom) {
        int max = 1 << zoom;
        if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max)
            return null;
        
        MapTile tile = tileCache.getTile(tileSource, tilex, tiley, zoom);
        if (tile == null) {
            tile = new MapTile(tileSource, tilex, tiley, zoom);
            tileCache.addTile(tile);
        }
        return tile;
    }

    public TileSource getTileSource() {
        return tileSource;
    }

    public void setTileSource(TileSource tileSource) {
        this.tileSource = tileSource;
    }
}

// License: GPL. For details, see Readme.txt file.
package gui.core.mapTileControl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import gui.is.interfaces.maptiles.MapTileCache;
import gui.is.interfaces.maptiles.TileSource;

/**
 * {@link MapTileCache} implementation that stores all {@link MapTile} objects in
 * memory up to a certain limit ({@link #getCacheSize()}). If the limit is
 * exceeded the least recently used {@link MapTile} objects will be deleted.
 *
 * @author Jan Peter Stotz
 */
public class MemoryMapTileCache implements MapTileCache {

    /**
     * Default cache size
     */
    private int cacheSize;

    protected final Map<String, MapTile> hash;

    /**
     * List of all tiles in their last recently used order,
     * Usefull when removing old tiles
     */
    protected final LinkedList<MapTile> lruTiles;

    /**
     * Constructs a new {@code MemoryTileCache}.
     */
    public MemoryMapTileCache() {
        this(200);
    }

    /**
     * Constructs a new {@code MemoryTileCache}.
     * @param cacheSize size of the cache
     */
    public MemoryMapTileCache(int cacheSize) {
        this.cacheSize = cacheSize;
        hash = new HashMap<>(cacheSize);
        lruTiles = new LinkedList<MapTile>();
    }

    @Override
    public synchronized void addTile(MapTile tile) {
    	if (hash.put(tile.getKey(), tile) == null) {
            // only if hash hadn't had the element, add it to LRU
            lruTiles.addFirst(tile);
            if (hash.size() > cacheSize || lruTiles.size() > cacheSize) {
                removeOldEntries();
            }
        }
    }

    @Override
    public synchronized MapTile getTile(TileSource source, int x, int y, int z) {
    	MapTile entry = hash.get(MapTile.getTileKey(source, x, y, z));
        if (entry == null)
            return null;
        lruTiles.remove(entry);
        lruTiles.addFirst(entry);
        return entry;
    }

    /**
     * Removes the least recently used tiles
     */
    protected synchronized void removeOldEntries() {
        try {
        	while (lruTiles.size() > cacheSize) {
        		removeEntry(lruTiles.getLast());
            }
        } catch (Exception e) {
            
        }
    }

    protected synchronized void removeEntry(MapTile entry) {
    	hash.remove(entry.getKey());
        lruTiles.remove(entry);
    }

    @Override
    public synchronized void clear() {
        hash.clear();
        lruTiles.clear();
    }

    @Override
    public synchronized int getTileCount() {
        return hash.size();
    }

    @Override
    public synchronized int getCacheSize() {
        return cacheSize;
    }

    /**
     * Changes the maximum number of {@link MapTile} objects that this cache holds.
     *
     * @param cacheSize
     *            new maximum number of tiles
     */
    public synchronized void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        if (hash.size() > cacheSize)
            removeOldEntries();
    }
}

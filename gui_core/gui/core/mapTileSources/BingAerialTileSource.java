package gui.core.mapTileSources;

import java.io.IOException;
import gui.is.mapTileSources.AbstractOsmTileSource;

public class BingAerialTileSource extends AbstractOsmTileSource {

    public BingAerialTileSource() {
        super("Bing Aerial Maps", "http://ecn.t2.tiles.virtualearth.net/tiles/", "BING");
    }

    @Override
    public int getMaxZoom() {
        return 22;
    }

    @Override
    public String getExtension() {
        return ("jpeg");
    }

    @Override
    public String getTilePath(int zoom, int tilex, int tiley) throws IOException {
        try {
            String quadtree = computeQuadTree(zoom, tilex, tiley);
            return "/tiles/a" + quadtree + "." + getExtension() + "?g=587";
        } catch (Exception e) {
            throw new IOException("Cannot load Bing attribution", e);
        }
    }

    static String computeQuadTree(int zoom, int tilex, int tiley) {
        StringBuilder k = new StringBuilder();
        for (int i = zoom; i > 0; i--) {
            char digit = 48;
            int mask = 1 << (i - 1);
            if ((tilex & mask) != 0) {
                digit += 1;
            }
            if ((tiley & mask) != 0) {
                digit += 2;
            }
            k.append(digit);
        }
        return k.toString();
    }
}
package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.geometry.BoundingBox;

import java.awt.*;
import java.awt.List;
import java.util.*;

/**
 * Created by Gebruiker on 2/18/2017.
 */
public class TileRule extends RenderRule {
    private Drawer drawer;

    @Override
    public void draw(RenderRule renderRule, Graphics2D g2d, Drawer drawer) {
        this.drawer=drawer;
        TileData tileData = (TileData)dataSource;
        java.util.List<Tile> tiles = getTiles(drawer.getRenderBox(),drawer.getViewModel().getZoomLevel());
        if (tiles!=null) {
            for (Tile tile : tiles) {
                Point tl = Utils.coordinateToPixels(new Coordinate(tile.getBoundingBox().getMinX(), tile.getBoundingBox().getMaxY()),drawer.getUnitSize(),drawer.getRenderBox());
                Point br = Utils.coordinateToPixels(new Coordinate(tile.getBoundingBox().getMaxX(), tile.getBoundingBox().getMinY()),drawer.getUnitSize(),drawer.getRenderBox());
                g2d.drawImage(tile.getImage(), tl.x, tl.y, br.x, br.y, 0, 0, tile.getImage().getWidth(), tile.getImage().getHeight(), null);
            }
        }
    }

    public java.util.List<Tile> getTiles(BoundingBox boundingBox, float zoomlvl) {
        java.util.List<Tile> tiles = null;
        int z = Math.round(zoomlvl);
        int minzoom = ((TileData) dataSource).minzoom;
        int maxzoom = ((TileData) dataSource).maxzoom;
        double extent = ((TileData) dataSource).extent;
        if (z >= minzoom && z <= maxzoom) {
            tiles = new ArrayList<>();
            double size = extent * 2 / Math.pow(2, z);

            int xt1 = (int) Math.floor((boundingBox.getMinX() + extent) / size);
            int xt2 = (int) Math.ceil((boundingBox.getMaxX() + extent) / size);

            int yt1 = (int) Math.floor((extent - boundingBox.getMaxY()) / size);
            int yt2 = (int) Math.ceil((extent - boundingBox.getMinY()) / size);

            for (int xt = xt1; xt < xt2; xt++) {
                for (int yt = yt1; yt < yt2; yt++) {
                    if (drawer.getDraw().isStop()) {
                        break;
                    }
                    Tile t = ((TileData) dataSource).getTile(xt, yt, z);
                    if (t != null) {
                        tiles.add(t);
                    }
                }
            }
        }
        return tiles;
    }
}

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
        if (drawer.getDrawthread().getRenderBox().intersects(((TileData)dataSource).getBounds())) {
            Tile[] tiles = ((TileData)dataSource).getTiles(drawer.getDrawthread().getRenderBox(),drawer.getViewModel().getZoomLevel());
            if (tiles!=null) {
                for (Tile tile : tiles) {
                    if (tile!=null) {
                        Point tl = Utils.coordinateToPixels(new Coordinate(tile.getBoundingBox().getMinX(), tile.getBoundingBox().getMaxY()), drawer.getUnitSize(), drawer.getDrawthread().getRenderBox());
                        Point br = Utils.coordinateToPixels(new Coordinate(tile.getBoundingBox().getMaxX(), tile.getBoundingBox().getMinY()), drawer.getUnitSize(), drawer.getDrawthread().getRenderBox());
                        if (tile.getImage() != null) {
                            g2d.drawImage(tile.getImage(), tl.x, tl.y, br.x, br.y, 0, 0, tile.getImage().getWidth(), tile.getImage().getHeight(), null);
                        } else {
                            g2d.setStroke(new BasicStroke(5f));
                            g2d.setColor(Color.white);
                            g2d.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
                            g2d.setColor(Color.black);
                            g2d.drawLine(tl.x, tl.y, br.x, br.y);
                            g2d.drawLine(br.x, tl.y, tl.x, br.y);
                            g2d.drawRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
                            g2d.drawString(tile.getTilePos()[0] + "," + tile.getTilePos()[1], tl.x + ((br.x - tl.x) / 2), tl.y + ((br.y - tl.y) / 2));
                        }
                    }
                }
            }
        }
    }
}

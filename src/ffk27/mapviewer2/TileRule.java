package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;

import java.awt.*;

/**
 * Created by Gebruiker on 2/18/2017.
 */
public class TileRule extends RenderRule {
    @Override
    public void draw(RenderRule renderRule, Graphics2D g2d, ViewModel viewModel) {
        TileData tileData = (TileData)dataSource;
        java.util.List<Tile> tiles = tileData.getTiles(viewModel.getBoundingBox(),viewModel.getZoomLevel());
        if (tiles!=null) {
            for (Tile tile : tiles) {
                Point tl = viewModel.coordinateToPixels(new Coordinate(tile.getBoundingBox().getMinX(), tile.getBoundingBox().getMaxY()));
                Point br = viewModel.coordinateToPixels(new Coordinate(tile.getBoundingBox().getMaxX(), tile.getBoundingBox().getMinY()));
                g2d.drawImage(tile.getImage(), tl.x, tl.y, br.x, br.y, 0, 0, tile.getImage().getWidth(), tile.getImage().getHeight(), null);
            }
        }
    }
}

package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.*;
import javafx.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class MapView extends JPanel {
    private ViewModel viewModel;
    private List<RenderRule> renderRules;
    private RasterImage mapImage;
    private Renderer renderer;

    public MapView(Coordinate coordinate, float zoomLevel, int srid) {
        viewModel = new ViewModel();
        renderRules = new ArrayList<>();
        viewModel.setMapCenter(coordinate);
        viewModel.setZoomLevel(zoomLevel);
        viewModel.setSrid(srid);
        new Controller(this);
    }

    public void changeMapCenter(Coordinate mapCenter) {
        viewModel.setMapCenter(mapCenter);
        repaint();
    }

    public void changeZoomLevel(float zoomLevel) {
        viewModel.setZoomLevel(zoomLevel);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        boolean redrawneeded=false;
        if (mapImage!=null) {
            Point tl = viewModel.coordinateToScreenPixels(new Coordinate(mapImage.getBoundingBox().getMinX(), mapImage.getBoundingBox().getMaxY()));
            Point br = viewModel.coordinateToScreenPixels(new Coordinate(mapImage.getBoundingBox().getMaxX(), mapImage.getBoundingBox().getMinY()));
            g.drawImage(mapImage.getImage(), tl.x, tl.y, br.x, br.y, 0, 0, mapImage.getImage().getWidth(), mapImage.getImage().getHeight(), null);
            if (!mapImage.getBoundingBox().equals(viewModel.getBoundingBox())) {
                redrawneeded=true;
            }
        }
        else {
            redrawneeded=true;
        }
        if (redrawneeded) {
            if (renderRules!=null && renderRules.size()>0) {
                draw();
            }
        }
    }

    private void draw() {
        if (renderer != null) {
            renderer.stopt();
        }
        BoundingBox bboxscreen = viewModel.getBoundingBox();
        renderer = new Renderer(this,bboxscreen,renderRules,viewModel);
    }

    public void updateMapImage(RasterImage rasterImage) {
        mapImage=rasterImage;
        repaint();
    }

    public void setRenderRules(List<RenderRule> renderRules) {
        this.renderRules = renderRules;
        repaint();
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    public void addRenderRule(RenderRule renderRule) {
        renderRules.add(renderRule);
        repaint();
    }
}

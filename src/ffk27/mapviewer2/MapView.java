package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.*;
import javafx.geometry.BoundingBox;

import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class MapView extends Canvas {
    private ViewModel viewModel;
    private List<RenderRule> renderRules;
    private RasterImage mapImage;
    private Drawer[] drawers;

    public MapView(Coordinate coordinate, float zoomLevel, int srid) {
        viewModel = new ViewModel();
        renderRules = new ArrayList<>();
        viewModel.setMapCenter(coordinate);
        viewModel.setZoomLevel(zoomLevel);
        viewModel.setSrid(srid);
        drawers=new Drawer[Runtime.getRuntime().availableProcessors()];
        for (int i=0; i<drawers.length; i++) {
            drawers[i] = new Drawer(this, renderRules);
        }
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
        BoundingBox bboxscreen = viewModel.getBoundingBox();
        double width = bboxscreen.getWidth()/drawers.length;
        for (int i=0; i<drawers.length; i++) {
            double minX = bboxscreen.getMinX()+width*i;
            BoundingBox bbox = new BoundingBox(minX,bboxscreen.getMinY(),width,bboxscreen.getHeight());
            drawers[i].renderArea(bbox,viewModel.getZoomLevel(),getWidth()/Runtime.getRuntime().availableProcessors(),getHeight(),viewModel.getUnitSize());
        }
    }

    public void updateMapImage() {
        BufferedImage bufferedImage = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
        mapImage=new RasterImage(bufferedImage,viewModel.getBoundingBox());
        Graphics g = bufferedImage.createGraphics();
        for (Drawer d : drawers) {
            if (d.getRasterImage() != null) {
                BoundingBox boundingBox = d.getRasterImage().getBoundingBox();
                Point tl = viewModel.coordinateToScreenPixels(new Coordinate(boundingBox.getMinX(), boundingBox.getMaxY()));
                Point br = viewModel.coordinateToScreenPixels(new Coordinate(boundingBox.getMaxX(), boundingBox.getMinY()));
                g.drawImage(d.getRasterImage().getImage(), tl.x, tl.y, br.x, br.y, 0, 0, d.getRasterImage().getImage().getWidth(), d.getRasterImage().getImage().getHeight(), null);
            }
        }
        repaint();
    }

    private void updateMap(RasterImage rasterImage) {
        repaint();
    }

    public List<RenderRule> getRenderRules() {
        return renderRules;
    }

    public void setRenderRules(List<RenderRule> renderRules) {
        this.renderRules = renderRules;
        for (int i=0; i<drawers.length; i++) {
            drawers[i].setRenderRules(renderRules);
        }
        repaint();
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    public Drawer[] getDrawers() {
        return drawers;
    }
}

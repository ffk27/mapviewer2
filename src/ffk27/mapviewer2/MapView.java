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
    private List<GeoDataSource> enabledDataSources;
    private RasterImage mapImage;
    private Drawer[] drawers;

    public MapView(Coordinate coordinate, float zoomLevel, int srid) {
        viewModel = new ViewModel();
        renderRules = new ArrayList<>();
        viewModel.setMapCenter(coordinate);
        viewModel.setZoomLevel(zoomLevel);
        viewModel.setSrid(srid);
        int availableProcessors = 1;//Runtime.getRuntime().availableProcessors();
        drawers=new Drawer[(int)Math.pow(Math.ceil(Math.sqrt(availableProcessors)),2)];
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
        //new DataCollect(bboxscreen).start();

        int sqrt = (int)Math.sqrt(drawers.length);
        double width = bboxscreen.getWidth()/sqrt;
        double height = bboxscreen.getHeight()/sqrt;
        for (int i=0; i<sqrt; i++) {
            double minY = bboxscreen.getMaxY()-height*(i+1);
            for (int i2=0; i2<sqrt; i2++) {
                Drawer drawer = drawers[i*sqrt+i2];
                double minX = bboxscreen.getMinX()+width*i2;
                BoundingBox bbox = new BoundingBox(minX,minY,width,height);
                if (drawer.getRasterImage() == null || (drawer.getRasterImage() != null && !drawer.getRasterImage().getBoundingBox().equals(bbox))) {
                    drawer.renderArea(bbox,viewModel.getZoomLevel(),getWidth()/sqrt,getHeight()/sqrt,viewModel.getUnitSize());
                }
            }
        }
    }

    public void updateMapImage() {
        BufferedImage bufferedImage = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
        mapImage=new RasterImage(bufferedImage,viewModel.getBoundingBox());
        Graphics2D g2d = bufferedImage.createGraphics();
        for (Drawer d : drawers) {
            if (d.getRasterImage() != null) {
                if (d.getRasterImage().getBoundingBox().equals(d.getRenderBox())) {
                    BoundingBox boundingBox = d.getRasterImage().getBoundingBox();
                    Point tl = viewModel.coordinateToScreenPixels(new Coordinate(boundingBox.getMinX(), boundingBox.getMaxY()));
                    Point br = viewModel.coordinateToScreenPixels(new Coordinate(boundingBox.getMaxX(), boundingBox.getMinY()));
                    g2d.drawImage(d.getRasterImage().getImage(), tl.x, tl.y, br.x, br.y, 0, 0, d.getRasterImage().getImage().getWidth(), d.getRasterImage().getImage().getHeight(), null);
                }
                else {
                    draw();
                    break;
                }
            }
        }
        repaint();
    }

    public void setRenderRules(List<RenderRule> renderRules) {
        this.renderRules = renderRules;
        for (int i=0; i<drawers.length; i++) {
            drawers[i].setRenderRules(renderRules);
        }
        enabledDataSources = GeoDataSource.getAllEnabledSources(renderRules);
        repaint();
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    public Drawer[] getDrawers() {
        return drawers;
    }

    public class DataCollect extends Thread {
        private BoundingBox bboxscreen;

        public DataCollect(BoundingBox bboxscreen) {
            this.bboxscreen = bboxscreen;
        }

        @Override
        public void run() {
            super.run();
            for (GeoDataSource dataSource : GeoDataSource.getAllEnabledSources(renderRules)) {
                System.out.println(dataSource.getName());
            }
        }
    }
}

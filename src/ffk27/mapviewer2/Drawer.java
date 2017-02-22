package ffk27.mapviewer2;

import javafx.geometry.BoundingBox;

import javax.swing.text.View;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created by Gebruiker on 2/20/2017.
 */
public class Drawer {
    private java.util.List<RenderRule> renderRules;
    private BoundingBox renderBox;
    private RasterImage rasterImage;
    private float zoomLevel;
    private int width,height;
    private MapView mapView;
    private double unitSize;
    private Draw drawthread;

    public Drawer(MapView mapView, java.util.List<RenderRule> renderRules) {
        this.mapView=mapView;
        this.renderRules = renderRules;
    }

    public boolean isLastDrawer(Draw draw) {
        for (Drawer d : mapView.getDrawers()) {
            if (d.getDrawthread()==null || !d.getDrawthread().equals(draw) && d.getDrawthread().isAlive()) {
                return false;
            }
        }
        return true;
    }

    public void done(RasterImage rasterImage) {
        this.rasterImage=rasterImage;
        if (isLastDrawer(drawthread)) {
            mapView.updateMapImage();
        }
    }

    public void renderArea(BoundingBox renderBox, float zoomLevel, int width, int height, double unitSize) {
        rasterImage=null;
        this.zoomLevel=zoomLevel;
        this.width=width;
        this.height=height;
        this.unitSize=unitSize;
        this.renderBox=renderBox;
        if (drawthread != null && drawthread.isAlive()) {
            drawthread.stopt();
        }
        drawthread = new Draw(renderBox);
        drawthread.start();
    }

    public BoundingBox getRenderBox() {
        return renderBox;
    }

    public ViewModel getViewModel() {
        return mapView.getViewModel();
    }

    public RasterImage getRasterImage() {
        return rasterImage;
    }

    public double getUnitSize() {
        return unitSize;
    }

    public List<RenderRule> getRenderRules() {
        return renderRules;
    }

    public void setRenderRules(List<RenderRule> renderRules) {
        this.renderRules = renderRules;
    }

    public Draw getDrawthread() {
        return drawthread;
    }

    public Drawer getDrawer() {
        return this;
    }

    public class Draw extends Thread {
        private boolean stop;
        private BoundingBox renderBox;

        public Draw(BoundingBox renderBox) {
            this.renderBox = renderBox;
        }

        @Override
        public void run() {
            super.run();
            draw();
        }

        public void draw() {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!stop) {
                RasterImage rasterImage = new RasterImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), renderBox);
                Graphics2D g2d = rasterImage.getImage().createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                for (RenderRule renderRule : renderRules) {
                    if (!stop) {
                        drawAllRules(renderRule, g2d);
                    }
                }
                done(rasterImage);
            }
        }

        private void drawAllRules(RenderRule renderRule, Graphics2D g2d) {
            if ((renderRule.getZoommin() == 0 && renderRule.getZoommax() == 0) || (zoomLevel >= renderRule.getZoommin() && zoomLevel <= renderRule.getZoommax())) {
                renderRule.draw(renderRule, g2d, getDrawer());
                if (renderRule.getRules() != null) {
                    for (RenderRule r : renderRule.getRules()) {
                        drawAllRules(r, g2d);
                    }
                }
            }
        }

        public boolean isStop() {
            return stop;
        }

        public void stopt() {
            stop = true;
        }

        public BoundingBox getRenderBox() {
            return renderBox;
        }
    }
}

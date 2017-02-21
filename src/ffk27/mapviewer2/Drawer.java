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
    private Draw drawzor;

    public Drawer(MapView mapView, java.util.List<RenderRule> renderRules) {
        this.mapView=mapView;
        this.renderRules = renderRules;
    }

    public boolean isLastDrawer(Draw draw) {
        for (Drawer d : mapView.getDrawers()) {
            if (d.getDraw()==null || !d.getDraw().equals(draw) && d.getDraw().isAlive()) {
                return false;
            }
        }
        return true;
    }

    public void done(RasterImage rasterImage) {
        this.rasterImage=rasterImage;
        mapView.updateMapImage();
    }

    public void renderArea(BoundingBox renderBox, float zoomLevel, int width, int height, double unitSize) {
        this.renderBox = renderBox;
        this.zoomLevel=zoomLevel;
        this.width=width;
        this.height=height;
        this.unitSize=unitSize;
        if (this.drawzor != null && this.drawzor.isAlive()) {
            this.drawzor.stopt();
        }
        this.drawzor = new Draw(this,width,height,zoomLevel,renderBox);
        this.drawzor.start();
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

    public float getZoomLevel() {
        return zoomLevel;
    }

    public Draw getDraw() {
        return drawzor;
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
}

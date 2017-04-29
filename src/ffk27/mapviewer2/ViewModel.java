package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.geometry.BoundingBox;

import java.awt.*;

/**
 * Created by Gebruiker on 2/19/2017.
 */
public class ViewModel {
    private Dimension screenSize;
    private Coordinate mapCenter;
    private float zoomLevel;
    public int minZoomlevel=0;
    public int maxZoomlevel=24;
    public float zoomSpeed=0.1f;
    private double scale = 156543.033906;
    private double unitSize;
    private int srid;
    private Point centerPoint;

    public int getSrid() {
        return srid;
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(float zoomLevel, java.util.List<RenderRule> renderRules) {
        this.zoomLevel = zoomLevel;
        unitSize=Math.pow(2,zoomLevel)/scale;
        updateStyles(renderRules);
    }

    public void updateStyles(java.util.List<RenderRule> renderRules) {
        for (RenderRule renderRule : renderRules) {
            updateStylesSizes(renderRule);
        }
    }

    public Coordinate pixelsToCoordinate(Point p) {
        return new Coordinate(mapCenter.x+Utils.pixelToUnit(p.x-centerPoint.x,unitSize),mapCenter.y-Utils.pixelToUnit(p.y-centerPoint.y,unitSize));
    }

    public Point coordinateToScreenPixels(Coordinate c) {
        Point point = new Point();
        point.setLocation(centerPoint.x + Utils.unitToPixel(c.x - mapCenter.x, unitSize),centerPoint.y - Utils.unitToPixel(c.y - mapCenter.y,unitSize));
        return point;
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(mapCenter.x-Utils.pixelToUnit(centerPoint.x,unitSize),mapCenter.y-Utils.pixelToUnit(centerPoint.y,unitSize),Utils.pixelToUnit(screenSize.getWidth(),unitSize),Utils.pixelToUnit(screenSize.getHeight(),unitSize));
    }

    public void setMapCenter(Coordinate mapCenter) {
        this.mapCenter = mapCenter;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public Coordinate getMapCenter() {
        return mapCenter;
    }

    public void setCenterPoint(Point centerPoint) {
        this.centerPoint = centerPoint;
    }

    public void setScreenSize(Dimension screenSize) {
        this.screenSize = screenSize;
        Point p = new Point();
        p.setLocation(screenSize.getWidth() / 2, screenSize.getHeight() / 2);
        centerPoint=p;
    }

    public Dimension getScreenSize() {
        return screenSize;
    }

    public double getUnitSize() {
        return unitSize;
    }

    private void updateStylesSizes(RenderRule renderRule) {
        if (renderRule instanceof VectorRenderRule) {
            for (Style style : ((VectorRenderRule) renderRule).getStyles()) {
                style.updateSizes(unitSize);
            }
        }
        if (renderRule.getRules()!=null) {
            for (RenderRule r : renderRule.getRules()) {
                updateStylesSizes(r);
            }
        }
    }
}

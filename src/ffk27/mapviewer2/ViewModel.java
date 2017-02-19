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
    private double scale = 160000;
    private double unitSize;
    private int srid;
    private Point centerPoint;

    public int getSrid() {
        return srid;
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
        unitSize=Math.pow(2,zoomLevel)/scale;
    }

    public double unitToPixel(double c) {
        return c * unitSize;
    }

    public double pixelToUnit(double p) {
        return p/unitSize;
    }


    public Coordinate pixelsToCoordinate(Point p) {
        return new Coordinate(mapCenter.x+pixelToUnit(p.x-centerPoint.x),mapCenter.y-pixelToUnit(p.y-centerPoint.y));
    }

    public Point coordinateToPixels(Coordinate c) {
        Point point = new Point();
        point.setLocation(centerPoint.x + unitToPixel(c.x - mapCenter.x),centerPoint.y - unitToPixel(c.y - mapCenter.y));
        return point;
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(mapCenter.x-pixelToUnit(centerPoint.x),mapCenter.y-pixelToUnit(centerPoint.y),pixelToUnit(screenSize.getWidth()),pixelToUnit(screenSize.getHeight()));
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
}

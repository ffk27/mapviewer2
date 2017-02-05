package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class MapView extends JPanel {
    public Coordinate mapCenter;
    private float zoomLevel;
    public float minZoomlevel=0.0f;
    public float maxZoomlevel=24.0f;
    public float zoomSpeed=0.1f;
    public double scale = 100000;
    private double unitSize;
    public int srid;
    private List<RenderRule> renderRules=new ArrayList<>();
    private Point centerPoint;

    public MapView() { mapCenter=new Coordinate(0,0); }

    public MapView(Coordinate coordinate, float zoomLevel, int srid) {
        mapCenter=coordinate;
        setZoomLevel(zoomLevel);
        this.srid=srid;
    }

    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
        unitSize=Math.pow(2,zoomLevel)/scale;
    }

    public void changeZoomLevel(float zoomLevel) {
        setZoomLevel(zoomLevel);
        repaint();
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

    public Point coordinateToScreen(Coordinate c) {
        Point point = new Point();
        point.setLocation(centerPoint.x + unitToPixel(c.x - mapCenter.x),centerPoint.y - unitToPixel(c.y - mapCenter.y));
        return point;
    }

    public List<RenderRule> getRenderRules() {
        return renderRules;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        centerPoint=new Point();
        centerPoint.setLocation(getWidth() / 2, getHeight() / 2);
    }
}

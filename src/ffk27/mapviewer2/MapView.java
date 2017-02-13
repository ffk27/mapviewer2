package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.*;
import javafx.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class MapView extends JPanel {
    private Coordinate mapCenter;
    private float zoomLevel;
    public float minZoomlevel=0.0f;
    public float maxZoomlevel=24.0f;
    public float zoomSpeed=0.1f;
    private double scale = 100000;
    private double unitSize;
    private int srid;
    private List<RenderRule> renderRules=new ArrayList<>();
    private Point centerPoint;

    public int getSrid() {
        return srid;
    }

    public MapView() { mapCenter=new Coordinate(0,0); }

    public MapView(Coordinate coordinate, float zoomLevel, int srid) {
        mapCenter=coordinate;
        setZoomLevel(zoomLevel);
        this.srid=srid;
    }

    public Coordinate getMapCenter() {
        return mapCenter;
    }

    public void setMapCenter(Coordinate mapCenter) {
        this.mapCenter = mapCenter;
        repaint();
    }

    public float getZoomLevel() {
        return zoomLevel;
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

    public BoundingBox getBoundingBox() {
        return new BoundingBox(mapCenter.x-pixelToUnit(centerPoint.x),mapCenter.y-pixelToUnit(centerPoint.y),pixelToUnit(getWidth()),pixelToUnit(getHeight()));
    }

    public List<RenderRule> getRenderRules() {
        return renderRules;
    }

    public void setRenderRules(List<RenderRule> renderRules) {
        this.renderRules = renderRules;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        centerPoint=new Point();
        centerPoint.setLocation(getWidth() / 2, getHeight() / 2);
        for (RenderRule renderRule : renderRules) {
            drawAllRules(renderRule,(Graphics2D)g);
        }
    }

    private void drawAllRules(RenderRule renderRule, Graphics2D g2d) {
        renderRule.draw(renderRule,g2d,this);
        if (renderRule.getRules()!=null) {
            for (RenderRule r : renderRule.getRules()) {
                drawAllRules(r,g2d);
            }
        }
    }
}

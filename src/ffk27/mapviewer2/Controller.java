package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;

import java.awt.*;
import java.awt.event.*;

/**
 * Created by Gebruiker on 2/6/2017.
 */
public class Controller implements MouseListener, MouseMotionListener, MouseWheelListener {
    private MapView mapView;
    private Point tempP;

    public Controller(MapView mapView) {
        this.mapView = mapView;
        mapView.addMouseListener(this);
        mapView.addMouseMotionListener(this);
        mapView.addMouseWheelListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println(mapView.pixelsToCoordinate(e.getPoint()));
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        tempP=null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (tempP==null) { tempP=e.getPoint(); }
        else {
            Coordinate tempC = mapView.pixelsToCoordinate(tempP);
            double difX = mapView.pixelsToCoordinate(e.getPoint()).x-tempC.x;
            double difY = mapView.pixelsToCoordinate(e.getPoint()).y-tempC.y;
            mapView.setMapCenter(new Coordinate(mapView.getMapCenter().x-difX,mapView.getMapCenter().y-difY));
            tempP=e.getPoint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getPreciseWheelRotation()>0) {
            zoomOut(e.getPoint());
        } else {
            zoomIn(e.getPoint());
        }
    }

    public void zoomIn(Point p) {
        if (mapView.getZoomLevel()<mapView.maxZoomlevel) {
            Coordinate c = mapView.pixelsToCoordinate(p);
            mapView.setZoomLevel(mapView.getZoomLevel()+mapView.zoomSpeed);
            Coordinate c2 = mapView.pixelsToCoordinate(p);
            mapView.setMapCenter(new Coordinate(mapView.getMapCenter().x+c.x-c2.x,mapView.getMapCenter().y+c.y-c2.y));
        }
    }

    public void zoomOut(Point p) {
        if (mapView.getZoomLevel()>mapView.minZoomlevel) {
            Coordinate c = mapView.pixelsToCoordinate(p);
            mapView.setZoomLevel(mapView.getZoomLevel()-mapView.zoomSpeed);
            Coordinate c2 = mapView.pixelsToCoordinate(p);
            mapView.setMapCenter(new Coordinate(mapView.getMapCenter().x-(c2.x-c.x),mapView.getMapCenter().y-(c2.y-c.y)));
        }
    }
}

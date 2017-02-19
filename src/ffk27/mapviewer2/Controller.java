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
        System.out.println(mapView.getViewModel().pixelsToCoordinate(e.getPoint()));
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
            Coordinate tempC = mapView.getViewModel().pixelsToCoordinate(tempP);
            double difX = mapView.getViewModel().pixelsToCoordinate(e.getPoint()).x-tempC.x;
            double difY = mapView.getViewModel().pixelsToCoordinate(e.getPoint()).y-tempC.y;
            mapView.changeMapCenter(new Coordinate(mapView.getViewModel().getMapCenter().x-difX,mapView.getViewModel().getMapCenter().y-difY));
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
        if (mapView.getViewModel().getZoomLevel()<mapView.getViewModel().maxZoomlevel) {
            Coordinate c = mapView.getViewModel().pixelsToCoordinate(p);
            mapView.getViewModel().setZoomLevel(mapView.getViewModel().getZoomLevel()+mapView.getViewModel().zoomSpeed);
            Coordinate c2 = mapView.getViewModel().pixelsToCoordinate(p);
            mapView.changeMapCenter(new Coordinate(mapView.getViewModel().getMapCenter().x+c.x-c2.x,mapView.getViewModel().getMapCenter().y+c.y-c2.y));
        }
    }

    public void zoomOut(Point p) {
        if (mapView.getViewModel().getZoomLevel()>mapView.getViewModel().minZoomlevel) {
            Coordinate c = mapView.getViewModel().pixelsToCoordinate(p);
            mapView.getViewModel().setZoomLevel(mapView.getViewModel().getZoomLevel()-mapView.getViewModel().zoomSpeed);
            Coordinate c2 = mapView.getViewModel().pixelsToCoordinate(p);
            mapView.changeMapCenter(new Coordinate(mapView.getViewModel().getMapCenter().x-(c2.x-c.x),mapView.getViewModel().getMapCenter().y-(c2.y-c.y)));
        }
    }
}

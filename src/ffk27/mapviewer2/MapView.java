package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.*;
import javafx.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class MapView extends JPanel implements ComponentListener {
    private ViewModel viewModel;
    private List<RenderRule> renderRules;

    public MapView(Coordinate coordinate, float zoomLevel, int srid) {
        addComponentListener(this);
        viewModel = new ViewModel();
        renderRules = new ArrayList<>();
        viewModel.setMapCenter(coordinate);
        viewModel.setZoomLevel(zoomLevel);
        viewModel.setSrid(srid);
        //Point p = new Point();
        //p.setLocation(getWidth() / 2, getHeight() / 2);
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
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (RenderRule renderRule : renderRules) {
            drawAllRules(renderRule,(Graphics2D)g);
        }
    }

    private void drawAllRules(RenderRule renderRule, Graphics2D g2d) {
        if ((renderRule.getZoommin() == 0 && renderRule.getZoommax() == 0) || (viewModel.getZoomLevel() >= renderRule.getZoommin() && viewModel.getZoomLevel() <= renderRule.getZoommax())) {
            renderRule.draw(renderRule, g2d, viewModel);
            if (renderRule.getRules() != null) {
                for (RenderRule r : renderRule.getRules()) {
                    drawAllRules(r, g2d);
                }
            }
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        viewModel.setScreenSize(getSize());
    }

    public List<RenderRule> getRenderRules() {
        return renderRules;
    }

    public void setRenderRules(List<RenderRule> renderRules) {
        this.renderRules = renderRules;
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}

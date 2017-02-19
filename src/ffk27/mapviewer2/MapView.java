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
public class MapView extends Canvas {
    private ViewModel viewModel;
    private List<RenderRule> renderRules;

    public MapView(Coordinate coordinate, float zoomLevel, int srid) {
        viewModel = new ViewModel();
        renderRules = new ArrayList<>();
        viewModel.setMapCenter(coordinate);
        viewModel.setZoomLevel(zoomLevel);
        viewModel.setSrid(srid);
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
        draw((Graphics2D)g);
    }

    private void draw(Graphics2D g2d) {
        for (RenderRule renderRule : renderRules) {
            drawAllRules(renderRule,g2d);
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

    public List<RenderRule> getRenderRules() {
        return renderRules;
    }

    public void setRenderRules(List<RenderRule> renderRules) {
        this.renderRules = renderRules;
    }

    public ViewModel getViewModel() {
        return viewModel;
    }
}

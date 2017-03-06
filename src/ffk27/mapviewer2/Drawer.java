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
public class Drawer extends Thread {
    private Renderer renderer;
    private BoundingBox boundingBox;
    private List<RenderRule> renderRules;
    private ViewModel viewModel;
    private boolean stop;
    private int width, height;
    private int x,y,z;

    public Drawer(Renderer renderer, BoundingBox boundingBox, List<RenderRule> renderRules, ViewModel viewModel, int width, int height, int x, int y, int z) {
        this.renderer=renderer;
        this.boundingBox = boundingBox;
        this.renderRules = renderRules;
        this.viewModel=viewModel;
        this.width=width;
        this.height=height;
        this.x=x;
        this.y=y;
        this.z=z;
        start();
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!stop) {
            RasterImage rasterImage = new RasterImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), boundingBox);
            Graphics2D g2d = rasterImage.getG2D();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            synchronized (renderRules) {
                for (RenderRule renderRule : renderRules) {
                    if (!stop) {
                        drawAllRules(renderRule, g2d);
                    }
                }
            }
            done(rasterImage);
        }
    }

    private void done(RasterImage rasterImage) {
        renderer.updateImage(rasterImage);
    }

    private void drawAllRules(RenderRule renderRule, Graphics2D g2d) {
        if ((renderRule.getZoommin() == 0 && renderRule.getZoommax() == 0) || (viewModel.getZoomLevel() >= renderRule.getZoommin() && viewModel.getZoomLevel() <= renderRule.getZoommax())) {
            renderRule.draw(renderRule, g2d, this);
            if (renderRule.getRules() != null) {
                for (RenderRule r : renderRule.getRules()) {
                    drawAllRules(r, g2d);
                }
            }
        }
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    public boolean isStop() {
        return stop;
    }

    public int[] getTilePos() {
        return new int[] {x,y,z};
    }

    public void stopt() {
        this.stop=true;
    }
}

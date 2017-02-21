package ffk27.mapviewer2;

import javafx.geometry.BoundingBox;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Gebruiker on 2/21/2017.
 */
public class Draw extends Thread {
    private Drawer drawer;
    private boolean stop;
    private int width,height;
    private float zoomLevel;
    private BoundingBox renderBox;

    public Draw(Drawer drawer, int width, int height, float zoomLevel, BoundingBox renderBox) {
        this.drawer=drawer;
        this.width=width;
        this.height=height;
        this.zoomLevel=zoomLevel;
        this.renderBox=renderBox;
    }

    @Override
    public void run() {
        super.run();
        draw();
    }

    public void draw() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!stop) {
            RasterImage rasterImage = new RasterImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), renderBox);
            Graphics2D g2d = rasterImage.getImage().createGraphics();
            for (RenderRule renderRule : drawer.getRenderRules()) {
                if (!stop) {
                    drawAllRules(renderRule, g2d);
                }
            }
            drawer.done(rasterImage);
        } else {
            //System.out.println("Vroegtijdig gestopt?");
        }
    }
    private void drawAllRules(RenderRule renderRule, Graphics2D g2d) {
        if ((renderRule.getZoommin() == 0 && renderRule.getZoommax() == 0) || (zoomLevel >= renderRule.getZoommin() && zoomLevel <= renderRule.getZoommax())) {
            renderRule.draw(renderRule, g2d, drawer);
            if (renderRule.getRules() != null) {
                for (RenderRule r : renderRule.getRules()) {
                    drawAllRules(r, g2d);
                }
            }
        }
    }


    public boolean isStop() {
        return stop;
    }

    public void stopt() {
        stop=true;
    }

    public BoundingBox getRenderBox() {
        return renderBox;
    }


}

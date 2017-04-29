package ffk27.mapviewer2;

import java.awt.*;

/**
 * Created by Gebruiker on 1/28/2017.
 */
public class CircleStyle extends Style {
    private String radius;
    private int radiusPixels;

    public CircleStyle(VectorRenderRule vectorRenderRule, Color fill, Color line, String strokeWidth, String radius) {
        super(vectorRenderRule, fill, line, strokeWidth);
        this.radius=radius;
    }

    public int getRadiusPixels() {
        return radiusPixels;
    }

    public void updateSizes(double unitSize) {
        super.updateSizes(unitSize);
        if (radius.endsWith("px")) {
            radiusPixels=Integer.parseInt(radius.split("px")[0]);
        }
        else {
            double r = Double.parseDouble(radius);
            radiusPixels = (int)Math.round(Utils.unitToPixel(r,unitSize));
        }
    }
}

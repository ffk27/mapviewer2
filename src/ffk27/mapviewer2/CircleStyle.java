package ffk27.mapviewer2;

import java.awt.*;

/**
 * Created by Gebruiker on 1/28/2017.
 */
public class CircleStyle extends Style {
    private float radius;

    public CircleStyle(VectorRenderRule vectorRenderRule, Color fill, Color line, Stroke stroke, float radius) {
        super(vectorRenderRule, fill, line, stroke);
        this.radius=radius;
    }

    public float getRadius() {
        return radius;
    }
}

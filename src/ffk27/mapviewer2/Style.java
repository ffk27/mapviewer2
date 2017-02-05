package ffk27.mapviewer2;

import java.awt.*;

/**
 * Created by ffk27 on 15-4-16.
 */
public class Style {
    private VectorRenderRule vectorRenderRule;
    private Color fill;
    private Color line;
    private Stroke stroke;

    public Style(VectorRenderRule vectorRenderRule, Color fill, Color line, Stroke stroke) {
        this.vectorRenderRule = vectorRenderRule;
        this.fill = fill;
        this.line = line;
        this.stroke = stroke;
    }

    public VectorRenderRule getStyleRule() {
        return vectorRenderRule;
    }

    public Color getFill() {
        return fill;
    }

    public Color getLine() {
        return line;
    }

    public Stroke getStroke() {
        return stroke;
    }
}

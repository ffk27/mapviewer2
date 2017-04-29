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
    private String strokeWidth;

    public Style(VectorRenderRule vectorRenderRule, Color fill, Color line, String strokeWidth) {
        this.vectorRenderRule = vectorRenderRule;
        this.fill = fill;
        this.line = line;
        this.strokeWidth = strokeWidth;
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

    public void updateSizes(double unitSize) {
        if (strokeWidth!=null) {
            if (strokeWidth.contains("px")) {
                stroke = new BasicStroke(Integer.parseInt(strokeWidth.split("px")[0]), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            } else {
                stroke = new BasicStroke(Math.round(Utils.unitToPixel(Double.parseDouble(strokeWidth), unitSize)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            }
        }
    }
}

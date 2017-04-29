package ffk27.mapviewer2;

import java.awt.*;

/**
 * Created by Gebruiker on 1/28/2017.
 */
public class TextStyle extends Style {
    private String format;
    private Font font;

    public TextStyle(VectorRenderRule vectorRenderRule, Color fill, Color line, String strokeWidth, String format, Font font) {
        super(vectorRenderRule, fill, line, strokeWidth);
        this.format=format;
        this.font=font;
    }

    public String getFormat() {
        return format;
    }

    public Font getFont() {
        return font;
    }

    @Override
    public void updateSizes(double unitSize) {
        super.updateSizes(unitSize);
    }
}

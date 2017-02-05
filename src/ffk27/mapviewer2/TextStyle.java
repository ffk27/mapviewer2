package ffk27.mapviewer2;

import java.awt.*;

/**
 * Created by Gebruiker on 1/28/2017.
 */
public class TextStyle extends Style {
    private String format;
    private Font font;

    public TextStyle(VectorRenderRule vectorRenderRule, Color fill, Color line, Stroke stroke, String format, Font font) {
        super(vectorRenderRule, fill, line, stroke);
        this.format=format;
        this.font=font;
    }

    public String getFormat() {
        return format;
    }
}
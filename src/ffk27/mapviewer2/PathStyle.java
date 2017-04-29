package ffk27.mapviewer2;

import java.awt.*;

/**
 * Created by Gebruiker on 3/10/2017.
 */
public class PathStyle extends Style {
    public PathStyle(VectorRenderRule vectorRenderRule, Color fill, Color line, String strokeWidth) {
        super(vectorRenderRule, fill, line, strokeWidth);
    }

    @Override
    public void updateSizes(double unitSize) {
        super.updateSizes(unitSize);
    }
}

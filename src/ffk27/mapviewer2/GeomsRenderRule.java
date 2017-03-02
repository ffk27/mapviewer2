package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Geometry;

import java.awt.*;

/**
 * Created by Gebruiker on 2/18/2017.
 */
public class GeomsRenderRule extends VectorRenderRule {
    public GeomsRenderRule(GeoDataSource geoDataSource) {
        super(geoDataSource);
    }

    @Override
    public void draw(RenderRule renderRule, Graphics2D g2d, Drawer drawer) {
        Geoms geoms = (Geoms)dataSource;
        for (Geometry geometry : geoms.getGeometries()) {
            drawGeom(geometry,null,g2d,drawer);
        }
    }
}

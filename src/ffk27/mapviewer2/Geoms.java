package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 2/18/2017.
 */
public class Geoms extends GeoDataSource {
    private List<Geometry> geometries;

    public Geoms(String name, int srid) {
        super(name, srid);
        geometries=new ArrayList<>();
    }

    public List<Geometry> getGeometries() {
        return geometries;
    }
}

package ffk27.mapviewer2;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public abstract class GeoDataSource {
    protected String name;
    protected int srid;

    public GeoDataSource(String name, int srid) {
        this.name = name;
        this.srid = srid;
    }

    public String getName() {
        return name;
    }

    public int getSrid() {
        return srid;
    }
}

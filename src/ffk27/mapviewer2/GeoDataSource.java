package ffk27.mapviewer2;

import java.util.ArrayList;
import java.util.List;

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

    public static List<GeoDataSource> getAllEnabledSources(List<RenderRule> renderRules) {
        List<GeoDataSource> dataSources = new ArrayList<>();
        for (RenderRule renderRule : renderRules) {
            dataSources.addAll(getSources(renderRule, null));
        }
        return dataSources;
    }

    private static List<GeoDataSource> getSources(RenderRule renderRule, List<GeoDataSource> dataSources) {
        if (renderRule.getDataSource()!=null && renderRule.isEnabled()) {
            if (dataSources==null) { dataSources = new ArrayList<>(); }
            if (!dataSources.contains(renderRule.getDataSource())) {
                dataSources.add(renderRule.getDataSource());
            }
        }
        if (renderRule.getRules()!=null) {
            for (RenderRule r : renderRule.getRules()) {
                dataSources = getSources(r,dataSources);
            }
        }
        return dataSources;
    }
}

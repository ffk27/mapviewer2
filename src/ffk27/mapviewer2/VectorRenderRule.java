package ffk27.mapviewer2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class VectorRenderRule extends RenderRule {
    private String[] attributes;
    private String statement;
    private List<Style> styles;
    private int geomType;

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public int getGeomType() {
        return geomType;
    }

    public void setGeomType(int geomType) {
        this.geomType = geomType;
    }

    public static VectorRenderRule createRandom(JDBCVectorData vectorData) {
        VectorRenderRule vectorRenderRule = new VectorRenderRule();
        vectorRenderRule.setDataSource(vectorData);
        Style s = new Style(vectorRenderRule,new Color(new Random().nextFloat(),new Random().nextFloat(),new Random().nextFloat(),1f),Color.BLACK,new BasicStroke(1f));
        List<Style> styles = new ArrayList<>();
        styles.add(s);
        vectorRenderRule.setStyles(styles);
        return vectorRenderRule;
    }
}

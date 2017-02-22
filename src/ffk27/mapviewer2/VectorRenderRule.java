package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import javafx.geometry.BoundingBox;

import java.awt.*;
import java.awt.geom.Path2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public abstract class VectorRenderRule extends RenderRule {
    protected String[] attributes;
    protected String[] statement;
    protected List<Style> styles;
    protected int geomType;

    protected void drawGeom(Geometry g, String[] labels, Graphics2D g2d, Drawer drawer) {
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        for (int i = 0; i < g.getNumGeometries(); i++) {
            if (g.getNumPoints() > 0) {
                String geometryType = g.getGeometryN(i).getGeometryType();
                Point screenpos;
                if (geometryType == "Point") {
                    screenpos = Utils.coordinateToPixels(g.getGeometryN(i).getCoordinate(),drawer.getUnitSize(),drawer.getDrawthread().getRenderBox() );
                    
                    path.moveTo(screenpos.x,screenpos.y);
                } else {
                    if (geometryType == "Polygon") {
                        com.vividsolutions.jts.geom.Polygon p = (com.vividsolutions.jts.geom.Polygon) g.getGeometryN(i);
                        screenpos = Utils.coordinateToPixels(p.getExteriorRing().getCoordinates()[0],drawer.getUnitSize(),drawer.getDrawthread().getRenderBox() );
                        path.moveTo(screenpos.x, screenpos.y);
                        for (int i2 = 1; i2 < p.getExteriorRing().getCoordinates().length; i2++) {
                            screenpos = Utils.coordinateToPixels(p.getExteriorRing().getCoordinates()[i2],drawer.getUnitSize(),drawer.getDrawthread().getRenderBox() );
                            path.lineTo(screenpos.x, screenpos.y);
                        }
                        for (int i2 = 0; i2 < p.getNumInteriorRing(); i2++) {
                            screenpos = Utils.coordinateToPixels(p.getInteriorRingN(i2).getCoordinates()[0],drawer.getUnitSize(),drawer.getDrawthread().getRenderBox() );
                            path.moveTo(screenpos.x, screenpos.y);
                            for (int i3 = 1; i3 < p.getInteriorRingN(i2).getCoordinates().length; i3++) {
                                screenpos = Utils.coordinateToPixels(p.getInteriorRingN(i2).getCoordinates()[i3],drawer.getUnitSize(),drawer.getDrawthread().getRenderBox() );
                                path.lineTo(screenpos.x, screenpos.y);
                            }
                        }
                    } else if (geometryType == "LineString") {
                        screenpos = Utils.coordinateToPixels(g.getGeometryN(i).getCoordinates()[0],drawer.getUnitSize(),drawer.getDrawthread().getRenderBox() );
                        path.moveTo(screenpos.x, screenpos.y);
                        for (int i2 = 1; i2 < g.getGeometryN(i).getCoordinates().length; i2++) {
                            screenpos = Utils.coordinateToPixels(g.getGeometryN(i).getCoordinates()[i2],drawer.getUnitSize(),drawer.getDrawthread().getRenderBox() );
                            path.lineTo(screenpos.x, screenpos.y);
                        }
                    }
                }
            }
        }
        for (Style style : styles) {
            drawGraphic(path, style,labels,g2d);
        }
    }

    protected void drawGraphic(Path2D path, Style style, String[] labels, Graphics2D g2d) {
        Point centroid = new Point((int)path.getBounds().getCenterX(),(int)path.getBounds().getCenterY());
        if (style instanceof CircleStyle) {
            CircleStyle cstyle = (CircleStyle) style;
            if (style.getFill() != null) {
                g2d.setColor(style.getFill());
                g2d.fillOval(centroid.x - (int)cstyle.getRadius(), centroid.y - (int)cstyle.getRadius(), (int)cstyle.getRadius() * 2, (int)cstyle.getRadius() * 2);
            }

            if (style.getLine() != null) {
                g2d.setColor(style.getLine());
                if (style.getStroke() != null) {
                    g2d.setStroke(style.getStroke());
                } else {
                    g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                }
                g2d.drawOval(centroid.x - (int)cstyle.getRadius(), centroid.y - (int)cstyle.getRadius(), (int)cstyle.getRadius() * 2, (int)cstyle.getRadius() * 2);
            }
        } else if (style instanceof TextStyle) {
            if (((TextStyle)style).getFormat()!=null && !((TextStyle)style).getFormat().isEmpty()) {
                String[] texts = ((TextStyle) style).getFormat().split("\\\\n");
                for (int i=0; i<texts.length; i++) {
                    String[] attrs = style.getStyleRule().getAttributes();
                    for (int i2 = 0; i2 < attrs.length; i2++) {
                        String attrname = attrs[i2];
                        if (texts[i].contains("{" + attrname + "}")) {
                            texts[i]=texts[i].replace("{" + attrname + "}", labels[i2]);
                        }
                    }
                }
                if (style.getFill() == null) {
                    g2d.setColor(Color.BLACK);
                } else {
                    g2d.setColor(style.getFill());
                }
                Font font = ((TextStyle)style).getFont();
                if (font!=null) {
                    g2d.setFont(font);
                }
                FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
                for (int i = 0; i < texts.length; i++) {
                    int y = (centroid.y - metrics.getHeight() / 2 * texts.length) + metrics.getAscent() + i * (metrics.getHeight()/2);
                    g2d.drawString(texts[i], centroid.x - metrics.stringWidth(texts[i]) / 2, y);
                }
            }
        }
        if (style instanceof Style) {
            if (style.getFill() != null) {
                g2d.setColor(style.getFill());
                g2d.fill(path);
            }

            if (style.getLine() != null) {
                g2d.setColor(style.getLine());
                if (style.getStroke() != null) {
                    g2d.setStroke(style.getStroke());
                } else {
                    g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                }
                g2d.draw(path);
            }
        }
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

    public static VectorRenderRule createRandom(GeoDataSource geoDataSource) {
        VectorRenderRule vectorRenderRule=null;
        if (geoDataSource instanceof JDBCVectorData) {
            vectorRenderRule=new JDBCRenderRule();
        }
        else {
            vectorRenderRule = new GeomsRenderRule();
        }
        vectorRenderRule.setDataSource(geoDataSource);
        Style s = new Style(vectorRenderRule,new Color(new Random().nextFloat(),new Random().nextFloat(),new Random().nextFloat(),1f),Color.BLACK,new BasicStroke(1f));
        List<Style> styles = new ArrayList<>();
        styles.add(s);
        vectorRenderRule.setStyles(styles);
        return vectorRenderRule;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public String[] getStatement() {
        return statement;
    }

    public void setStatement(String[] statement) {
        this.statement = statement;
    }
}

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
public class VectorRenderRule extends RenderRule {
    private String[] attributes;
    private String statement;
    private List<Style> styles;
    private int geomType;

    @Override
    public void draw(RenderRule renderRule, Graphics2D g2d, MapView mapView) {
        try {
            if (zoommin > zoommax) {
                zoommax=mapView.maxZoomlevel;
            }
            if ((zoommin == 0 && zoommax == 0) || (mapView.getZoomLevel() >= zoommin && mapView.getZoomLevel() <= zoommax)) {
                if (((VectorRenderRule)renderRule).getStyles().size()>0) {
                    JDBCVectorData jdbcVectorData = (JDBCVectorData)renderRule.getDataSource();
                    JDBCConnection.DBType dbType = jdbcVectorData.getJdbcDataTable().getJdbcConnection().getDbType();
                    String geometry_column = jdbcVectorData.getGeometryColumn();
                    BoundingBox boundingb = mapView.getBoundingBox();
                    double minX = boundingb.getMinX();
                    double minY = boundingb.getMinY();
                    double maxX = boundingb.getMaxX();
                    double maxY = boundingb.getMaxY();
                    com.vividsolutions.jts.geom.Polygon bboxG = new GeometryFactory().createPolygon(new Coordinate[]{new Coordinate(minX, maxY), new Coordinate(maxX, maxY), new Coordinate(maxX, minY), new Coordinate(minX, minY), new Coordinate(minX, maxY)});
                    String attributes = "";
                    String[] attrarray = getAllAttributes((VectorRenderRule)renderRule, null);
                    if (attrarray != null && attrarray.length > 0) {
                        for (String attribute : attrarray) {
                            attributes += "," + attribute;
                        }
                    }
                    String bbox = "ST_Transform(ST_GeomFromText('" + bboxG.toText() + "'," + mapView.getSrid() + ")," + mapView.getSrid() + ")";
                    String query = "SELECT ST_AsBinary(ST_Transform(" + geometry_column + "," + mapView.getSrid() + ")) " + attributes + " FROM " + jdbcVectorData.getJdbcDataTable().getTableName() + " WHERE ";
                    if (dbType == JDBCConnection.DBType.H2 || dbType == JDBCConnection.DBType.PostgreSQL) {
                        query += geometry_column + " && " + bbox;
                    } else if (dbType == JDBCConnection.DBType.SQLite) {
                        query += "ROWID IN (SELECT ROWID FROM SpatialIndex WHERE f_table_name='" + jdbcVectorData.getJdbcDataTable().getTableName() + "' AND f_geometry_column='" + geometry_column + "' AND search_frame=" + bbox + ")";
                    }
                    if (dbType == JDBCConnection.DBType.H2 || dbType == JDBCConnection.DBType.SQLite) {
                        query += " AND (ST_Intersects(" + geometry_column + "," + bbox + "))";
                    }
                    if (((VectorRenderRule)renderRule).getGeomType() != 0) {
                        query += " AND ST_GeometryType(" + geometry_column + ")='";
                        String gtype = Utils.geomTypeCode2Name(((VectorRenderRule)renderRule).getGeomType());
                        if (dbType == JDBCConnection.DBType.PostgreSQL) {
                            query += "ST_";
                        } else if (dbType == JDBCConnection.DBType.SQLite) {
                            gtype = gtype.toUpperCase();
                        }
                        query += gtype + "'";
                    }
                    query += getAllStatements((VectorRenderRule)renderRule, "");
                    query += ";";
                    System.out.println(query);

                    Statement stmt = jdbcVectorData.getJdbcDataTable().getJdbcConnection().getConnection().createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        String[] labels = null;
                        String[] attrs = getAllAttributes((VectorRenderRule)renderRule, null);
                        if (attrs != null && attrs.length > 0) {
                            labels = new String[attrs.length];
                            for (int i = 0; i < attrs.length; i++) {
                                labels[i] = rs.getString(i + 2);
                            }
                        }
                        drawGeom(new WKBReader().read(rs.getBytes(1)), (VectorRenderRule)renderRule, labels,g2d,mapView);
                    }
                    rs.close();
                    stmt.close();
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
    }

    private String getAllStatements(VectorRenderRule vectorRenderRule, String statement) {
        if (vectorRenderRule.getStatement()!=null) {
            statement = " AND (" + vectorRenderRule.getStatement() + ")" + statement;
        }
        if (vectorRenderRule.getParent()!=null) {
            statement = getAllStatements((VectorRenderRule)vectorRenderRule.getParent(),statement);
        }
        return statement;
    }

    private String[] getAllAttributes(VectorRenderRule vectorRenderRule, String attributes[]) {
        String[] attrs=null;
        if (vectorRenderRule.getAttributes()!=null) {
            if (attributes != null) {
                attrs=new String[vectorRenderRule.getAttributes().length+attributes.length];
                for (int i=0; i< vectorRenderRule.getAttributes().length; i++) {
                    attrs[i]=vectorRenderRule.getAttributes()[i];
                }
                for (int i=vectorRenderRule.getAttributes().length; i<vectorRenderRule.getAttributes().length+attributes.length; i++) {
                    attrs[i]=attributes[i-vectorRenderRule.getAttributes().length];
                }
            }
            else {
                attrs = vectorRenderRule.getAttributes();
            }
        }
        else if (attributes!=null) {
            attrs=attributes;
        }
        if (vectorRenderRule.getParent()!=null) {
            attrs = getAllAttributes((VectorRenderRule)vectorRenderRule.getParent(),attrs);
        }
        return attrs;
    }

    private void drawGeom(Geometry g, VectorRenderRule renderRule, String[] labels, Graphics2D g2d, MapView mapView) {
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        for (int i = 0; i < g.getNumGeometries(); i++) {
            if (g.getNumPoints() > 0) {
                String geometryType = g.getGeometryN(i).getGeometryType();
                Point screenpos;
                if (geometryType == "Point") {
                    screenpos = mapView.coordinateToScreen(g.getGeometryN(i).getCoordinate());
                    path.moveTo(screenpos.x,screenpos.y);
                } else {
                    if (geometryType == "Polygon") {
                        com.vividsolutions.jts.geom.Polygon p = (com.vividsolutions.jts.geom.Polygon) g.getGeometryN(i);
                        screenpos = mapView.coordinateToScreen(p.getExteriorRing().getCoordinates()[0]);
                        path.moveTo(screenpos.x, screenpos.y);
                        for (int i2 = 1; i2 < p.getExteriorRing().getCoordinates().length; i2++) {
                            screenpos = mapView.coordinateToScreen(p.getExteriorRing().getCoordinates()[i2]);
                            path.lineTo(screenpos.x, screenpos.y);
                        }
                        for (int i2 = 0; i2 < p.getNumInteriorRing(); i2++) {
                            screenpos = mapView.coordinateToScreen(p.getInteriorRingN(i2).getCoordinates()[0]);
                            path.moveTo(screenpos.x, screenpos.y);
                            for (int i3 = 1; i3 < p.getInteriorRingN(i2).getCoordinates().length; i3++) {
                                screenpos = mapView.coordinateToScreen(p.getInteriorRingN(i2).getCoordinates()[i3]);
                                path.lineTo(screenpos.x, screenpos.y);
                            }
                        }
                    } else if (geometryType == "LineString") {
                        screenpos = mapView.coordinateToScreen(g.getGeometryN(i).getCoordinates()[0]);
                        path.moveTo(screenpos.x, screenpos.y);
                        for (int i2 = 1; i2 < g.getGeometryN(i).getCoordinates().length; i2++) {
                            screenpos = mapView.coordinateToScreen(g.getGeometryN(i).getCoordinates()[i2]);
                            path.lineTo(screenpos.x, screenpos.y);
                        }
                    }
                }
            }
        }
        for (Style style : renderRule.getStyles()) {
            drawGraphic(path, style,labels,g2d);
        }
    }

    private void drawGraphic(Path2D path, Style style, String[] labels, Graphics2D g2d) {
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
                    String[] attrs = getAllAttributes(style.getStyleRule(),null);
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

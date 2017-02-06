package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import javafx.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class MapView extends JPanel {
    private Coordinate mapCenter;
    private float zoomLevel;
    public float minZoomlevel=0.0f;
    public float maxZoomlevel=24.0f;
    public float zoomSpeed=0.1f;
    private double scale = 100000;
    private double unitSize;
    private int srid;
    private List<RenderRule> renderRules=new ArrayList<>();
    private Point centerPoint;
    private BoundingBox boundingBox;
    private Graphics2D g2d;

    public MapView() { mapCenter=new Coordinate(0,0); }

    public MapView(Coordinate coordinate, float zoomLevel, int srid) {
        mapCenter=coordinate;
        setZoomLevel(zoomLevel);
        this.srid=srid;
    }

    public Coordinate getMapCenter() {
        return mapCenter;
    }

    public void setMapCenter(Coordinate mapCenter) {
        this.mapCenter = mapCenter;
        repaint();
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
        unitSize=Math.pow(2,zoomLevel)/scale;
    }

    public void changeZoomLevel(float zoomLevel) {
        setZoomLevel(zoomLevel);
        repaint();
    }

    public double unitToPixel(double c) {
        return c * unitSize;
    }

    public double pixelToUnit(double p) {
        return p/unitSize;
    }


    public Coordinate pixelsToCoordinate(Point p) {
        return new Coordinate(mapCenter.x+pixelToUnit(p.x-centerPoint.x),mapCenter.y-pixelToUnit(p.y-centerPoint.y));
    }

    public Point coordinateToScreen(Coordinate c) {
        Point point = new Point();
        point.setLocation(centerPoint.x + unitToPixel(c.x - mapCenter.x),centerPoint.y - unitToPixel(c.y - mapCenter.y));
        return point;
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(mapCenter.x-pixelToUnit(centerPoint.x),mapCenter.y-pixelToUnit(centerPoint.y),pixelToUnit(getWidth()),pixelToUnit(getHeight()));
    }

    public List<RenderRule> getRenderRules() {
        return renderRules;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        centerPoint=new Point();
        centerPoint.setLocation(getWidth() / 2, getHeight() / 2);
        boundingBox=getBoundingBox();
        g2d=(Graphics2D)g;
        for (RenderRule renderRule : renderRules) {
            draw(renderRule);
        }
    }

    public void draw(RenderRule renderRule) {
        try {
            if (renderRule.getZoommin() > renderRule.getZoommax()) {
                renderRule.setZoommax(maxZoomlevel);
            }
            if ((renderRule.getZoommin() == 0 && renderRule.getZoommax() == 0) || (zoomLevel >= renderRule.getZoommin() && zoomLevel <= renderRule.getZoommax())) {
                if (renderRule.getDataSource() instanceof JDBCVectorData) {
                    if (((VectorRenderRule)renderRule).getStyles().size()>0) {
                        JDBCVectorData jdbcVectorData = (JDBCVectorData)renderRule.getDataSource();
                        JDBCConnection.DBType dbType = jdbcVectorData.getJdbcDataTable().getJdbcConnection().getDbType();
                        String geometry_column = jdbcVectorData.getGeometryColumn();

                        double minX = boundingBox.getMinX();
                        double minY = boundingBox.getMinY();
                        double maxX = boundingBox.getMaxX();
                        double maxY = boundingBox.getMaxY();
                        com.vividsolutions.jts.geom.Polygon bboxG = new GeometryFactory().createPolygon(new Coordinate[]{new Coordinate(minX, maxY), new Coordinate(maxX, maxY), new Coordinate(maxX, minY), new Coordinate(minX, minY), new Coordinate(minX, maxY)});
                        String attributes = "";
                        String[] attrarray = getAllAttributes((VectorRenderRule)renderRule, null);
                        if (attrarray != null && attrarray.length > 0) {
                            for (String attribute : attrarray) {
                                attributes += "," + attribute;
                            }
                        }
                        String bbox = "ST_Transform(ST_GeomFromText('" + bboxG.toText() + "'," + srid + ")," + srid + ")";
                        String query = "SELECT ST_AsBinary(ST_Transform(" + geometry_column + "," + srid + ")) " + attributes + " FROM " + jdbcVectorData.getJdbcDataTable().getTableName() + " WHERE ";
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
                            drawGeom(new WKBReader().read(rs.getBytes(1)), (VectorRenderRule)renderRule, labels);
                        }
                        rs.close();
                        stmt.close();
                    }

                }
                if (renderRule.getRules() != null) {
                    for (RenderRule r : renderRule.getRules()) {
                        draw(r);
                    }
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

    private void drawGraphic(Path2D path, Style style, String[] labels) {
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
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
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

    private void drawGeom(Geometry g, VectorRenderRule renderRule, String[] labels) {
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        for (int i = 0; i < g.getNumGeometries(); i++) {
            if (g.getNumPoints() > 0) {
                String geometryType = g.getGeometryN(i).getGeometryType();
                Point screenpos;
                if (geometryType == "Point") {
                    screenpos = coordinateToScreen(g.getGeometryN(i).getCoordinate());
                    path.moveTo(screenpos.x,screenpos.y);
                } else {
                    if (geometryType == "Polygon") {
                        com.vividsolutions.jts.geom.Polygon p = (com.vividsolutions.jts.geom.Polygon) g.getGeometryN(i);
                        screenpos = coordinateToScreen(p.getExteriorRing().getCoordinates()[0]);
                        path.moveTo(screenpos.x, screenpos.y);
                        for (int i2 = 1; i2 < p.getExteriorRing().getCoordinates().length; i2++) {
                            screenpos = coordinateToScreen(p.getExteriorRing().getCoordinates()[i2]);
                            path.lineTo(screenpos.x, screenpos.y);
                        }
                        for (int i2 = 0; i2 < p.getNumInteriorRing(); i2++) {
                            screenpos = coordinateToScreen(p.getInteriorRingN(i2).getCoordinates()[0]);
                            path.moveTo(screenpos.x, screenpos.y);
                            for (int i3 = 1; i3 < p.getInteriorRingN(i2).getCoordinates().length; i3++) {
                                screenpos = coordinateToScreen(p.getInteriorRingN(i2).getCoordinates()[i3]);
                                path.lineTo(screenpos.x, screenpos.y);
                            }
                        }
                    } else if (geometryType == "LineString") {
                        screenpos = coordinateToScreen(g.getGeometryN(i).getCoordinates()[0]);
                        path.moveTo(screenpos.x, screenpos.y);
                        for (int i2 = 1; i2 < g.getGeometryN(i).getCoordinates().length; i2++) {
                            screenpos = coordinateToScreen(g.getGeometryN(i).getCoordinates()[i2]);
                            path.lineTo(screenpos.x, screenpos.y);
                        }
                    }
                }
            }
        }
        for (Style style : renderRule.getStyles()) {
            drawGraphic(path, style,labels);
        }
    }
}

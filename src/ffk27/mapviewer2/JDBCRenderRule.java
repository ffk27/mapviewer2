package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import javafx.geometry.BoundingBox;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Gebruiker on 2/18/2017.
 */
public class JDBCRenderRule extends VectorRenderRule {
    public JDBCRenderRule(GeoDataSource geoDataSource) {
        super(geoDataSource);
    }

    @Override
    public void draw(RenderRule renderRule, Graphics2D g2d, Drawer drawer) {
        if (((VectorRenderRule)renderRule).getStyles().size()>0) {
            JDBCVectorData jdbcVectorData = (JDBCVectorData) renderRule.getDataSource();
            if (jdbcVectorData != null) {
                try {
                    JDBCConnection.DBType dbType = jdbcVectorData.getJdbcDataTable().getJdbcConnection().getDbType();
                    String geometry_column = jdbcVectorData.getGeometryColumn();
                    double minX = drawer.getBoundingBox().getMinX();
                    double minY = drawer.getBoundingBox().getMinY();
                    double maxX = drawer.getBoundingBox().getMaxX();
                    double maxY = drawer.getBoundingBox().getMaxY();
                    com.vividsolutions.jts.geom.Polygon bboxG = new GeometryFactory().createPolygon(new Coordinate[]{new Coordinate(minX, maxY), new Coordinate(maxX, maxY), new Coordinate(maxX, minY), new Coordinate(minX, minY), new Coordinate(minX, maxY)});
                    String attributes = "";
                    String[] attrarray = ((JDBCRenderRule) renderRule).getAttributes();
                    if (attrarray != null && attrarray.length > 0) {
                        for (String attribute : attrarray) {
                            attributes += "," + attribute;
                        }
                    }
                    String bbox = "ST_Transform(ST_GeomFromText('" + bboxG.toText() + "'," + drawer.getViewModel().getSrid() + ")," + drawer.getViewModel().getSrid() + ")";
                    String query = "SELECT ST_AsBinary(ST_Transform(" + geometry_column + "," + drawer.getViewModel().getSrid() + ")) " + attributes + " FROM " + jdbcVectorData.getJdbcDataTable().getTableName() + " WHERE ";

                    if (dbType == JDBCConnection.DBType.H2 || dbType == JDBCConnection.DBType.PostgreSQL) {
                        query += geometry_column + " && " + bbox;
                    } else if (dbType == JDBCConnection.DBType.SQLite) {
                        query += "ROWID IN (SELECT ROWID FROM SpatialIndex WHERE f_table_name='" + jdbcVectorData.getJdbcDataTable().getTableName() + "' AND f_geometry_column='" + geometry_column + "' AND search_frame=" + bbox + ")";
                    }

                    if (dbType == JDBCConnection.DBType.H2 || dbType == JDBCConnection.DBType.SQLite) {
                        query += " AND (ST_Intersects(" + geometry_column + "," + bbox + "))";
                    }

                    if (((VectorRenderRule) renderRule).getGeomType() != 0) {
                        query += " AND ST_GeometryType(" + geometry_column + ")='";
                        String gtype = Utils.geomTypeCode2Name(((VectorRenderRule) renderRule).getGeomType());
                        if (dbType == JDBCConnection.DBType.PostgreSQL) {
                            query += "ST_";
                        } else if (dbType == JDBCConnection.DBType.SQLite) {
                            gtype = gtype.toUpperCase();
                        }
                        query += gtype + "'";
                    }
                    if (((JDBCRenderRule) renderRule).getStatement() != null) {
                        for (String stmt : ((JDBCRenderRule) renderRule).getStatement()) {
                            query += " AND (" + stmt + ")";
                        }
                    }
                    query += ";";
                    //System.out.println(query);
                    Connection connection = jdbcVectorData.getJdbcDataTable().getJdbcConnection().getConnection();
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        if (drawer.isStop()) {
                            break;
                        }
                        String[] labels = null;
                        if (attrarray != null && attrarray.length > 0) {
                            labels = new String[attrarray.length];
                            for (int i = 0; i < attrarray.length; i++) {
                                labels[i] = rs.getString(i + 2);
                            }
                        }
                        drawGeom(new WKBReader().read(rs.getBytes(1)), labels, g2d, drawer);
                    }
                    rs.close();
                    stmt.close();
                } catch(SQLException se){
                    se.printStackTrace();
                } catch(ParseException pe){
                    pe.printStackTrace();
                }

            } else {
                System.out.println("Error: No or invalid data source");
            }
        }
    }
}

package ffk27.mapviewer2;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class JDBCVectorData extends GeoDataSource {
    private JDBCDataTable jdbcDataTable;
    private String geometryColumn;

    public JDBCVectorData(String name, int srid, JDBCDataTable jdbcDataTable, String geometryColumn) throws Exception {
        super(name,srid);
        this.jdbcDataTable = jdbcDataTable;
        if (jdbcDataTable.columnNameExist(geometryColumn)) {
            this.geometryColumn = geometryColumn;
        } else {
            throw new Exception("Geometry column does not exist");
        }
    }

    public JDBCDataTable getJdbcDataTable() {
        return jdbcDataTable;
    }

    public String getGeometryColumn() {
        return geometryColumn;
    }
}

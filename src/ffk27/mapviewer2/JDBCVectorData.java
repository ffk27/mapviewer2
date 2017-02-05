package ffk27.mapviewer2;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class JDBCVectorData extends GeoDataSource {
    private JDBCDataTable jdbcDataTable;
    private String geometryColumn;

    public JDBCVectorData(String name, JDBCDataTable jdbcDataTable, String geometryColumn) throws Exception {
        this.name=name;
        this.jdbcDataTable = jdbcDataTable;
        if (jdbcDataTable.columnNameExist(geometryColumn)) {
            this.geometryColumn = geometryColumn;
        } else {
            throw new Exception("Geometry column does not exist");
        }
    }
}

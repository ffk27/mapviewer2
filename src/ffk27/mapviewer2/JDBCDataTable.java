package ffk27.mapviewer2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public class JDBCDataTable extends DataTable {
    private JDBCConnection jdbcConnection;
    private String tableName;

    public JDBCDataTable(JDBCConnection jdbcConnection, String tableName) {
        this.jdbcConnection = jdbcConnection;
        this.tableName = tableName;
        recieveColumns();
    }

    public JDBCConnection getJdbcConnection() {
        return jdbcConnection;
    }

    public String getTableName() {
        return tableName;
    }

    private void recieveColumns() {
        try {
            Statement stmt = jdbcConnection.getConnection().createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM "+tableName+";");
            int columnCount= resultSet.getMetaData().getColumnCount();
            columnNames=new String[columnCount];
            columnTypes=new String[columnCount];
            for (int i=0; i<columnCount; i++) {
                columnNames[i]=resultSet.getMetaData().getColumnName(i+1);
                columnTypes[i]=resultSet.getMetaData().getColumnTypeName(i+1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

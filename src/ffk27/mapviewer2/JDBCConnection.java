package ffk27.mapviewer2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by ffk27 on 2/5/2017.
 */
public class JDBCConnection {
    private DBType dbType;
    private Connection connection;
    private String url;
    private Properties properties;

    public JDBCConnection(DBType dbType, String url, Properties properties) {
        this.dbType = dbType;
        this.url = url;
        this.properties = properties;
        connect();
    }

    public boolean isConnected() {
        if (connection!=null) {
            try {
                return !connection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean connect() {
        try {
            connection = DriverManager.getConnection(url,properties);
            return isConnected();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean reconnect() {
        close();
        return connect();
    }

    public void close() {
        if (connection!=null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public DBType getDbType() {
        return dbType;
    }

    public enum DBType {
        H2,
        PostgreSQL,
        SQLite
    }
}

package me.desht.sensibletoolbox.core.storage;

import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.core.ItemRegistry;

import java.io.File;
import java.sql.*;

public class DBStorage {
    private final Connection connection;

    public DBStorage() throws SQLException, ClassNotFoundException {
        connection = connectSQLite();
        setupTable();
    }

    public Connection getConnection() {
        return connection;
    }

    private Connection connectSQLite() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        File dbFile = new File(SensibleToolboxPlugin.getInstance().getDataFolder(), "blocks.db");
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }

    private void setupTable() throws SQLException {
        createTableIfNotExists("blocks",
                "world_id VARCHAR(36) NOT NULL," +
                        "x INTEGER NOT NULL," +
                        "y INTEGER NOT NULL," +
                        "z INTEGER NOT NULL," +
                        "type VARCHAR(" + ItemRegistry.MAX_ITEM_ID_LENGTH + ") NOT NULL," +
                        "data TEXT NOT NULL," +
                        "PRIMARY KEY (world_id,x,y,z)");
    }

    private void createTableIfNotExists(String tableName, String ddl) throws SQLException {
        String fullName = makeTableName(tableName);
        Statement stmt = connection.createStatement();
        try {
            if (!tableExists(fullName)) {
                stmt.executeUpdate("CREATE TABLE " + fullName + "(" + ddl + ")");
            }
        } catch (SQLException e) {
            LogUtils.warning("can't execute " + stmt + ": " + e.getMessage());
            throw e;
        }
    }

    static String makeTableName(String base) {
        return SensibleToolboxPlugin.getInstance().getConfig().getString("database.table_prefix", "stb_") + base;
    }

    private boolean tableExists(String table) throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, table, null);
        return tables.next();
    }
}

package io.github.thebusybiscuit.sensibletoolbox.core.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.core.STBItemRegistry;
import me.desht.dhutils.text.LogUtils;

/**
 * This class is responsible for connecting to our database.
 * 
 * @author desht
 * @author TheBusyBiscuit
 * 
 * @see LocationManager
 *
 */
class DatabaseManager {

    private final Logger logger;
    private final Connection connection;

    public DatabaseManager(@Nonnull Logger logger) throws SQLException {
        this.logger = logger;
        connection = connectToSQLite();
        setupTable();
    }

    @Nonnull
    public Connection getConnection() {
        return connection;
    }

    @Nonnull
    private Connection connectToSQLite() throws SQLException {
        logger.info("Connecting to local database...");
        File file = new File(SensibleToolboxPlugin.getInstance().getDataFolder(), "blocks.db");

        try {
            // Class.forName(...) is no longer required as of JDBC 4.0+
            return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (Exception | LinkageError x) {
            logger.log(Level.SEVERE, x, () -> "Could not connect to local database: \"jdbc:sqlite:" + file.getAbsolutePath() + "\"");
            throw new IllegalStateException("Database connection could not be established.");
        }
    }

    private void setupTable() throws SQLException {
        createTableIfNotExists("blocks", "world_id VARCHAR(36) NOT NULL," + "x INTEGER NOT NULL," + "y INTEGER NOT NULL," + "z INTEGER NOT NULL," + "type VARCHAR(" + STBItemRegistry.MAX_ITEM_ID_LENGTH + ") NOT NULL," + "data TEXT NOT NULL," + "PRIMARY KEY (world_id,x,y,z)");
    }

    @ParametersAreNonnullByDefault
    private void createTableIfNotExists(String tableName, String ddl) throws SQLException {
        String fullName = getFullTableName(tableName);

        try (Statement stmt = connection.createStatement()) {
            if (!hasTable(fullName)) {
                stmt.executeUpdate("CREATE TABLE " + fullName + "(" + ddl + ")");
            }
        } catch (SQLException e) {
            LogUtils.warning("can't create table " + fullName + ": " + e.getMessage());
            throw e;
        }
    }

    static String getFullTableName(@Nonnull String base) {
        return SensibleToolboxPlugin.getInstance().getConfig().getString("database.table_prefix", "stb_") + base;
    }

    private boolean hasTable(@Nonnull String table) throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, table, null);
        return tables.next();
    }
}

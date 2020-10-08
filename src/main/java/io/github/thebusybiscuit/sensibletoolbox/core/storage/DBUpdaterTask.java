package io.github.thebusybiscuit.sensibletoolbox.core.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.dhutils.Debugger;

class DBUpdaterTask implements Runnable {

    private final LocationManager manager;
    private final PreparedStatement insertStmt;
    private final PreparedStatement updateStmt;
    private final PreparedStatement deleteStmt;

    public DBUpdaterTask(@Nonnull LocationManager manager) throws SQLException {
        this.manager = manager;

        String tableName = DatabaseManager.getFullTableName("blocks");
        insertStmt = manager.getDatabaseConnection().getConnection().prepareStatement("INSERT INTO " + tableName + " VALUES(?,?,?,?,?,?)");
        updateStmt = manager.getDatabaseConnection().getConnection().prepareStatement("UPDATE " + tableName + " SET data = ?, type = ? WHERE world_id = ? and x = ? and y = ? and z = ?");
        deleteStmt = manager.getDatabaseConnection().getConnection().prepareStatement("DELETE FROM " + tableName + " WHERE world_id = ? and x = ? and y = ? and z = ?");
    }

    @Override
    public void run() {
        Debugger.getInstance().debug("database writer thread starting");
        boolean finished = false;

        while (!finished) {
            try {
                // block till available
                UpdateRecord rec = manager.getUpdateRecord();
                int n = 0;
                Debugger.getInstance().debug("DB write [" + rec + "]");

                switch (rec.getOp()) {
                case FINISH:
                    finished = true;
                    break;
                case COMMIT:
                    manager.getDatabaseConnection().getConnection().commit();
                    break;
                case INSERT:
                    insertStmt.setString(1, rec.getWorldID().toString());
                    insertStmt.setInt(2, rec.getX());
                    insertStmt.setInt(3, rec.getY());
                    insertStmt.setInt(4, rec.getZ());
                    insertStmt.setString(5, rec.getType());
                    insertStmt.setString(6, rec.getData());
                    n = insertStmt.executeUpdate();
                    break;
                case UPDATE:
                    updateStmt.setString(1, rec.getData());
                    updateStmt.setString(2, rec.getType());
                    updateStmt.setString(3, rec.getWorldID().toString());
                    updateStmt.setInt(4, rec.getX());
                    updateStmt.setInt(5, rec.getY());
                    updateStmt.setInt(6, rec.getZ());
                    n = updateStmt.executeUpdate();
                    break;
                case DELETE:
                    deleteStmt.setString(1, rec.getWorldID().toString());
                    deleteStmt.setInt(2, rec.getX());
                    deleteStmt.setInt(3, rec.getY());
                    deleteStmt.setInt(4, rec.getZ());
                    n = deleteStmt.executeUpdate();
                    break;
                }

                Debugger.getInstance().debug("DB write complete: rows modified = " + n);
            } catch (InterruptedException e) {
                SensibleToolboxPlugin.getInstance().getLogger().log(Level.SEVERE, "Database Thread was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (SQLException e) {
                SensibleToolboxPlugin.getInstance().getLogger().log(Level.SEVERE, "An Exception has disturbed the Database Thread", e);
            }
        }

        Debugger.getInstance().debug("database writer thread exiting");
    }
}

package com.codecool.dao.sql;

import com.codecool.dao.IExpLevelDao;
import com.codecool.model.ExpLevel;
import com.codecool.model.Student;
import com.codecool.model.UserCredentials;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExpLevelSQL implements IExpLevelDao {
    private BasicConnectionPool connectionPool;

    public ExpLevelSQL(BasicConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void addExpLevel(ExpLevel expLevel) {
        try {
            Connection connection = connectionPool.getConnection();
            addExpLevelToDatabase(expLevel, connection);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void addExpLevelToDatabase(ExpLevel expLevel, Connection connection) throws SQLException {
        try (PreparedStatement stmtInsertExpLevelData = connection.prepareStatement(
                "INSERT INTO experience_levels(name, experience_amount) VALUES(?, ?)")) {
            insertExpLevel(stmtInsertExpLevelData, expLevel);
        }
    }

    private void insertExpLevel(PreparedStatement stmtInsertExpLevelData, ExpLevel expLevel) throws SQLException {
        stmtInsertExpLevelData.setString(1, expLevel.getName());
        stmtInsertExpLevelData.setString(2, expLevel.getName());
        stmtInsertExpLevelData.executeUpdate();
    }

    @Override
    public void updateExpLevel(ExpLevel expLevel) {
        try {
            Connection connection = connectionPool.getConnection();
            updateExpLevelData(connection, expLevel);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void updateExpLevelData(Connection connection, ExpLevel expLevel) throws SQLException {
        try (PreparedStatement stmtUpdateExpLevelData = connection.prepareStatement(
                "UPDATE experience_levels SET experience_amount = ? WHERE name = ?")) {
            updateExpLevelInDatabase(stmtUpdateExpLevelData, expLevel);
        }
    }

    private void updateExpLevelInDatabase(PreparedStatement stmtUpdateExpLevelData, ExpLevel expLevel) throws SQLException {
        stmtUpdateExpLevelData.setInt(1, expLevel.getExpAmount());
        stmtUpdateExpLevelData.setString(2, expLevel.getName());
    }

    @Override
    public void removeLastExpLevel() {
        try {
            Connection connection = connectionPool.getConnection();
            removeLevelFromDatabase(connection);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void removeLevelFromDatabase(Connection connection) throws SQLException {
        try(PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM users  WHERE id = (SELECT * FROM experience_levels\n" +
                        "ORDER BY name DESC\n" +
                        "LIMIT 1)")) {
            stmt.executeUpdate();
        }
    }

    @Override
    public List<ExpLevel> getAllExpLevels() {
        List<ExpLevel> expLevels = new ArrayList<>();
        try {
            Connection connection = connectionPool.getConnection();
            addExpLevels(expLevels, connection);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
        return expLevels;
    }

    private void addExpLevels(List<ExpLevel> expLevels, Connection connection) throws SQLException {
        try(PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM experience_levels")) {
            addExpLevelToList(stmt, expLevels);
        }
    }

    private void addExpLevelToList(PreparedStatement stmt, List<ExpLevel> expLevels) throws SQLException{
        try (ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                ExpLevel expLevel = getSingleExpLevel(resultSet);
                expLevels.add(expLevel);
            }
        }
    }

    private ExpLevel getSingleExpLevel(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("name");
        int experienceAmount = resultSet.getInt("experience_amount");
        return new ExpLevel(name, experienceAmount);
    }

    @Override
    public ExpLevel getExpLevel(int expLevelId) {
        return null;
    }

    
}

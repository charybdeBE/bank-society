package be.charybde.bank.db.sqlite;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionHandler {
    private String DBPath = "db.db";
    private Connection connection = null;
    private Statement statement = null;

    public ConnectionHandler(String dBPath) {
        DBPath = dBPath;
    }

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            statement = connection.createStatement();
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void close() {
        try {
            connection.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
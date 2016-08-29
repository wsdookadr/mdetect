package com.mdetect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteStore {
	public String dbPath = null;
	public Connection connection = null;
	
	public String dbSchema = "";
	public SqliteStore() {
		dbPath = System.getenv("HOME") + "/.mdetect.db";
        try {
          connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch(SQLException e) {
          System.err.println(e.getMessage());
          System.exit(-1);
        }
    }
	
	public void createSchema() {
		String schema = Utils.getResource("/create_schema.sql");
		try {
			connection.createStatement().executeUpdate(schema);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void add() {
		try {
			PreparedStatement pstmt = connection.prepareStatement("INSERT INTO (");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

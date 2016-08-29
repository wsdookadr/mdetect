package com.mdetect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteStore {
	public String dbPath = null;
	public Connection connection = null;
	
	public String queryInsert = null;
	public String querySchema = null;
	
	public String dbSchema = "";
	public SqliteStore() {
		querySchema = Utils.getResource("/create_schema.sql");
		queryInsert = Utils.getResource("/insert_checksum.sql");
		dbPath = System.getenv("HOME") + "/.mdetect.db";
        try {
          connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
          connection.setAutoCommit(false);
        } catch(SQLException e) {
          System.err.println(e.getMessage());
          System.exit(-1);
        }
    }
	
	public void createSchema() {
		
		try {
			connection.createStatement().executeUpdate(querySchema);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addChecksum(GitFileDTO f, String gtag) {
		try {
			/* need to speed this up */
			PreparedStatement pstmt = connection.prepareStatement(queryInsert);
			pstmt.setString(1, f.getPath());
			pstmt.setString(2, f.getSha1());
			pstmt.setString(3, gtag);
			pstmt.setInt(4, f.getFileSize());
			pstmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}

package com.mdetect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


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
			commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Adds file metadata (including a checksum) to the sqlite database.
	 */
	public void addChecksum(GitFileDTO f, String gtag) {
		try {
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
	
	public boolean hasChecksum(String sha1) {
		try {
			PreparedStatement p = prepare("SELECT COUNT(*) FROM gitfiles WHERE sha1 = ?;");
			p.setString(1, sha1);
			ResultSet r = p.executeQuery();
			r.next();
			boolean result = (r.getInt(1) > 0);
			r.close();
			p.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public PreparedStatement prepare(String query) {
		try {
			return connection.prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}

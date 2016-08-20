package com.mdetect;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Analyzer {
	/*
	 * This class contains minimal logic that does a set of
	 * very specific sqlite operations.
	 * 
	 * In particular, it connects to a sqlite database, and then it
	 * precomputes a series of characteristics of known codebases
	 * and stores them in the sqlite database.
	 * 
	 * For example, the most common class/function/method/variable
	 * names, file sizes and checksums.
	 * 
	 */
	public String dbPath = null;
	public Connection connection = null;
	public  Analyzer() {
		dbPath = "/tmp/sample.db";
        try
        {
          connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
          Statement statement = connection.createStatement();
          statement.setQueryTimeout(30);
        }
        catch(SQLException e)
        {
          System.err.println(e.getMessage());
          System.exit(-1);
        };
    }
	
	void run() {
		
	}
	
	
}

package com.mdetect;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

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
	 * Note: Because Git has already computed hashes of files, we will
	 * retrieve those and use them.
	 * 
	 * Note: The database should store filePath, size, fileChecksum
	 * 
	 */
	public String dbPath = null;
	public Connection connection = null;
	
	public String dbSchema = "";
	public  Analyzer() {
		dbPath = "/tmp/sample.db";
        try {
          connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch(SQLException e) {
          System.err.println(e.getMessage());
          System.exit(-1);
        };
        
    }

	/*
	 * Acquire file hashes, sizes and paths from Git repositories
	 */
	void acquireData() {
		IOFileFilter gitFilter = FileFilterUtils.notFileFilter(
				FileFilterUtils.and(
						FileFilterUtils.directoryFileFilter(),
						FileFilterUtils.nameFileFilter(".git"))
				);
		File dir = new File("dir");
		try {
			System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			try {
				System.out.println("file: " + file.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/*
	 * cleans up the entire database.
	 */
	void reset() {
		
	}
	
}

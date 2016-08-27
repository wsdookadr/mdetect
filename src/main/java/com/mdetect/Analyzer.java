package com.mdetect;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

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
	 * Note: We only consider releases (so Git tags).
	 * 
	 * Note: Because Git has already computed hashes of 
	 * 		 files, we will retrieve those and use them.
	 * 
	 * Note: The database should have a table like this
	 * 		 commit, filePath, size, fileChecksum
	 * 
	 * Note: The sha1 that Git stores (which is accessible via git ls-files -s)
	 * 		 is computed on (length, contents)
	 * 		 http://stackoverflow.com/a/24283352/827519
	 * 		 http://stackoverflow.com/a/7225329/827519
	 * 
	 * Note: Since this is PHP code, we have the source readily available.
	 * 		 Compared to languages that compile to binaries [1] 
	 * 		 (for example C or C++) where compilers might not produce the
	 * 		 same binary for one build, the situation here is much 
	 * 		 better, we can check the source.
	 * 
	 * 		 [1] https://reproducible-builds.org/docs/checksums/
	 * 
	 * Note: This does follow a bit the logic of debsums
	 * 
	 */
	public String dbPath = null;
	public Connection connection = null;
	
	public String dbSchema = "";
	public Analyzer() {
		dbPath = "/tmp/sample.db";
        try {
          connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch(SQLException e) {
          System.err.println(e.getMessage());
          System.exit(-1);
        };
        
    }
	
	/*
	 * Find Git repositories (to retrieve checksums)
	 * 
	 * Note: even though the SuffixFileFilter is created
	 * 		 according to apache commons documentation, it doesn't
	 * 		 work properly. so an additional check
	 * 		 is done when enumerating the results to filter
	 * 		 only the .git directories. 
	 */
	List<String> findGitRepos(String dirPath) {
		File dir = new File(dirPath);
		IOFileFilter gitDirFilter = (IOFileFilter) FileFilterUtils.suffixFileFilter(".git");
		IOFileFilter notFile = FileFilterUtils.notFileFilter(TrueFileFilter.INSTANCE);
		IOFileFilter compositeFilter = FileFilterUtils.and(notFile, gitDirFilter);
		
		List<File> files = (List<File>) FileUtils.listFilesAndDirs(dir,compositeFilter,DirectoryFileFilter.INSTANCE);
		List<String> results = new ArrayList<String>();
		for(File f: files) {
			try {
				if(!f.getCanonicalPath().endsWith("/.git"))
					continue;
				
				String gitStripped = f.getCanonicalPath().replace("/.git", "");
				System.out.println(gitStripped);
				results.add(gitStripped);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return results;
	}

	/*
	 * Acquire file hashes, sizes and paths from Git repositories.
	 */
	List<String> findFilesToAnalyze(String dirPath) {
		IOFileFilter gitFilter = FileFilterUtils.notFileFilter(
						FileFilterUtils.nameFileFilter(".git")
				);
		File dir = new File(dirPath);
		String[] phpExt = new String[] {"php"};
		IOFileFilter phpFilter = new SuffixFileFilter(phpExt, IOCase.INSENSITIVE);
		List<File> files = (List<File>) FileUtils.listFiles(dir, phpFilter, gitFilter);
		List<String> results = new ArrayList<String>();
		for (File f : files) {
			try {
				results.add(f.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return results;
	}
	
	void _do() {
		/* 
		 * overview:
		 * 
		 * do the work for first tag
		 * git reset to each other tag
		 *     see what changed
		 * 	   get data for what changed
		 * 	   put it in the db
		 */
	}

	/*
	 * cleans up the entire database.
	 */
	void reset() {
		
	}
	
}

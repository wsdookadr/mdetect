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
	
	public Analyzer() {
		
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
	
}

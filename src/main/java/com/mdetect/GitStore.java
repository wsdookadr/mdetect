package com.mdetect;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.dircache.DirCacheTree;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitStore {

	
	public GitStore() {
		
	}
	/*
	 * Currently lists all files in the git repository
	 * and their respective object ids (sha1 with a prepended fixed string)
	 * 
	 */
	public void listHashes(String gitRepoPath) {
		Git git = null;
		try {
			git = Git.open(new File(gitRepoPath));
			Repository rep = git.getRepository();
			DirCache dc = rep.readDirCache();
		    DirCacheTree ct = dc.getCacheTree(false);
			DirCacheIterator di = new DirCacheIterator(dc);
			try (TreeWalk tw = new TreeWalk(rep)) {
				tw.addTree(di);
				tw.setRecursive(true);
				while (tw.next()) {
				    if (tw.isSubtree()) {
				        System.out.println("dir: " + tw.getPathString());
				        //tw.enterSubtree();
				    } else {
				    	
				    	String pathString = tw.getPathString();
				    	String sha1 = dc.getEntry(pathString).getObjectId().getName();
				        System.out.println("sha1: " + sha1 + " file: " + tw.getPathString());
				    }
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}

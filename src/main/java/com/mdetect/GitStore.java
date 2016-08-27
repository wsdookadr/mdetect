package com.mdetect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.dircache.DirCacheTree;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitStore {
	public Git git = null;
	public String gitRepoPath;
	public GitStore(String gitRepoPath) {
		this.gitRepoPath = gitRepoPath;
		try {
			git = Git.open(new File(gitRepoPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * Currently lists all files in the git repository
	 * and their respective object ids (sha1 with a prepended fixed string)
	 * 
	 */
	
	public List<String> getAllTags() {
		List<String> results = new ArrayList<String>();
		List<Ref> call;
		try {
			call = git.tagList().call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			return results;
		}
		System.out.println(call.size());
		Repository repository = git.getRepository();
		for (Ref ref : call) {
			try {
				ObjectId tagObjId = ref.getObjectId();
				String tagName = ref.getName();
				String tagCommit = tagObjId.getName();
				System.out.println("Tag: " + ref + " " + tagName + " " + tagCommit);
				LogCommand log = git.log();
				
				/*
				Ref peeledRef = repository.peel(ref);
				if (peeledRef.getPeeledObjectId() != null) {
					log.add(peeledRef.getPeeledObjectId());
				} else {
					log.add(ref.getObjectId());
				}
				
				Iterable<RevCommit> logs = log.call();
				for (RevCommit rev : logs) {
					//System.out.println("Commit: " + rev);
				}
				*/
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
		return results;
	}
	
	
	public void listHashes() {
		try {
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
				        tw.enterSubtree();
				    } else {
				    	String pathString = tw.getPathString();
				    	String sha1 = dc.getEntry(pathString).getObjectId().getName();
				        System.out.println("sha1: " + sha1 + " file: " + pathString);
				    }
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

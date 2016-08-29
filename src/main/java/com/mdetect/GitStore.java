package com.mdetect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.dircache.DirCacheTree;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

/*
 * This class has functionality for accessing a Git repository
 * (using the jgit library) and extracts required information from it.
 * 
 * The assumption made is the tags of the Git repository are expected
 * to be the releases for that software project.
 * 
 */
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
	public List<GitTagDTO> getAllTags() {
		List<GitTagDTO> results = new ArrayList<GitTagDTO>();
		List<Ref> call;
		try {
			call = git.tagList().call();
		} catch (Exception e) {
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
				GitTagDTO t = new GitTagDTO(tagName, tagCommit);
				results.add(t);
				System.out.println("Tag: " + ref + " " + tagName + " " + tagCommit);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
		return results;
	}

	/*
	 * Receives commit as a string. Checks out that specific commit
	 * and gets information about files and their object ids (sha1)
	 * at that commit.
	 * 
	 */
	public LinkedBlockingQueue<GitFileDTO> listHashes(String sCommit) {
		LinkedBlockingQueue<GitFileDTO> results = new LinkedBlockingQueue<GitFileDTO>();
		ObjectId oCommit = null;
		Repository rep = git.getRepository();
		// get commit (or HEAD if it's not specified)
		if(sCommit == null) {
			try {
				oCommit = rep.resolve(Constants.HEAD);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			oCommit = ObjectId.fromString(sCommit);
		}
		// switch to that commit
		try {
			git.checkout().setName(oCommit.getName()).call();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		// get file listing
		try {
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
				    	int fileSize = dc.getEntry(pathString).getLength();
				    	ObjectId oid = dc.getEntry(pathString).getObjectId();
				    	String sha1 = oid.getName();
				    	GitFileDTO fo = new GitFileDTO(fileSize,pathString,sha1);
				    	results.put(fo);
				    }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
}

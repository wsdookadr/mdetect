package com.mdetect;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/*
 * This iterator will go through all the git repos,
 * all their tags, and return every file
 * at every tagged version. It will exclude all 
 * the duplicates at repository-level (the duplicates being
 * those files that haven't changed from one tag to the next).
 */

public class GitFileIterator implements Iterator<Pair<GitFileDTO, String>> {

	/* list of all repositories found at that path */
	List<String> listRepos = null;
	/* per repo tags */
	List<GitTagDTO> listTags  = null;
	/* per tag GitFileDTOs */
	List<GitFileDTO> listGitFiles = null;

	Iterator<String> nextRepo = null;
	Iterator<GitTagDTO> nextTag  = null;
	Iterator<GitFileDTO> nextGitFile = null;

	/* the current repository being processed */
	GitStore currentRepo = null;
	/* current tag in the git repo */
	String currentTag = null;
	/* per repo seen set */
	HashSet<String> dupeSet = null;
	/* used to store the next available non-duplicate file */
	GitFileDTO preNext = null;

	
	public GitFileIterator(String knownFilesPath) {
		dupeSet = new HashSet<String>();
		listRepos = FileScanUtils.findGitRepos(knownFilesPath);
		nextRepo = listRepos.iterator();
		currentRepo = new GitStore(nextRepo.next());
		currentRepo.reset();
		listTags = currentRepo.getAllTags();
		nextTag = listTags.iterator();
		currentTag = nextTag.next().getTagCommit();
		listGitFiles = currentRepo.listHashes(currentTag);
		nextGitFile = listGitFiles.iterator();
		System.out.println(listRepos.size());
	}

	@Override
	public boolean hasNext() {
		preNext = precomputeNext();
		return nextGitFile.hasNext() || nextTag.hasNext() || nextRepo.hasNext();
	}

	/*
	 * switch to the first tag of a new repo,
	 * and load the files
	 */
	private void incRepo() {
		currentRepo = new GitStore(nextRepo.next());
		currentRepo.reset();
		listTags = currentRepo.getAllTags();
		nextTag = listTags.iterator();
		currentTag = nextTag.next().getTagCommit();
		listGitFiles = (List<GitFileDTO>) currentRepo.listHashes(currentTag);
		nextGitFile = listGitFiles.iterator();
	}

	/*
	 * switch to a new tag, and load the files
	 */
	private void incTag() {
		currentTag = nextTag.next().getTagCommit();
		listGitFiles = (List<GitFileDTO>) currentRepo.listHashes(currentTag);
		nextGitFile = listGitFiles.iterator();
	}

	/*
	 * duplicate check that returns the next non-duplicate
	 * file at repository level.
	 * (it skips over the duplicates).
	 * 
	 */
	private GitFileDTO nextNonDuplicateFile() {
		GitFileDTO f = nextGitFile.next();

		if(f == null)
			return f;

		String dupeKey = f.getPath() + f.getSha1();
		if(dupeSet.contains(dupeKey)) {
			return precomputeNext();
		} else {
			dupeSet.add(dupeKey);
			return f;
		}
	}

	/*
	 * this is called in hasNext() because
	 * it will jump over duplicates. when this happens, hasNext() will
	 * refer to a different node than next().
	 * 
	 * in order to avoid that, we call precomputeNext() in hasNext, store
	 * the value, and return it when next() is called.
	 * 
	 */
	public GitFileDTO precomputeNext() {
		/*
		 * no more files or tags or repos, we're done
		 */
		if(!nextGitFile.hasNext() && !nextTag.hasNext() && !nextRepo.hasNext()) {
			return null;
		}

		/*
		 * there are more files, return one
		 */
		if(nextGitFile.hasNext()) {
			return nextNonDuplicateFile();
		}

		/*
		 * there are more tags, switch to a new tag
		 * and return a file
		 */
		if(nextTag.hasNext()) {
			incTag();
			return nextNonDuplicateFile();
		}

		/*
		 * there are more repos, switch
		 * to a new repo's first tag
		 * and return a file.
		 * 
		 * the duplicate hashmap is reset
		 * for each new repository.
		 */
		if(nextRepo.hasNext()) {
			dupeSet.clear();
			incRepo();
			return nextGitFile.next();
		}
		return null;
	}

	@Override
	public Pair<GitFileDTO, String> next() {
		return new ImmutablePair<GitFileDTO, String>(preNext, currentTag);
	}

}

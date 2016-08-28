package com.mdetect;

import java.util.HashMap;

public class GitFileDTO {
	private int fileSize;
	private String path;
	private String sha1;

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSha1() {
		return sha1;
	}

	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	GitFileDTO(int fileSize, String path, String sha1) {
		this.fileSize = fileSize;
		this.path = path;
		this.sha1 = sha1;
	}
	
	public String toString() {
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("fileSize", Integer.toString(fileSize));
		h.put("path", path);
		h.put("sha1", sha1);
		return h.toString();
	}
}

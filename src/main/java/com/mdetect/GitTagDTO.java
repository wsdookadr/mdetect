package com.mdetect;

public class GitTagDTO {
	private String tagCommit;
	private String tagName;
	public String getTagCommit() {
		return tagCommit;
	}
	public void setTagCommit(String tagCommit) {
		this.tagCommit = tagCommit;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
	GitTagDTO(String tagName, String tagCommit) {
		this.tagName = tagName;
		this.tagCommit = tagCommit;
	}
}

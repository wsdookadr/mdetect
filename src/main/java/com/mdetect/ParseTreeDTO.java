package com.mdetect;

import org.w3c.dom.Document;

public class ParseTreeDTO {
	Document d = null;
	public Document getD() {
		return d;
	}
	public void setD(Document d) {
		this.d = d;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
	String filePath = "";
	String checkSum = "";
	
	ParseTreeDTO(Document d, String filePath, String checkSum) {
		this.d = d;
		this.filePath = filePath;
		this.checkSum = checkSum;
	}
	
	
	
}

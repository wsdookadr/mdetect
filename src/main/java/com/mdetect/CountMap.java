package com.mdetect;

import java.util.HashMap;

public class CountMap extends HashMap<String,Integer> {
	/*
	 * increase the count for this key
	 */
	public void add(String o) {
		int count;
		if(this.containsKey(o)) {
			count = ((Integer) this.get(o)).intValue() + 1; 
		} else {
			count = 1;
		}
		
		super.put(o, count);
	}

}

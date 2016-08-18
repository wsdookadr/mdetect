package com.mdetect;

public class Detector {
	/*
	 * So far, we want to cover these:
	 * - does not contain common names from 
	 *   Joomla/Wordpress/Drupal ; instead contains names 
	 *   that appear to be random (high) 
	 * - excessive use of chr() (medium)
	 * - excessive use of ord() (medium)
	 * - makes use of hex-escaped characters (low)
	 * - excessive use of string concatenation (high)
	 * - long base64 encoded strings (high)
	 * - use of the base64_decode PHP function (high)
	 * 
	 */
	
	public Detector() {
		
	}
}

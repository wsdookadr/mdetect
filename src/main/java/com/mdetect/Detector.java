package com.mdetect;

public class Detector {
	/*
	 * Note: The grammar works for PHP up to 5.6
	 * 
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
	 * - presence of eval() (high)
	 */
	
	public Detector() {
		
	}
}

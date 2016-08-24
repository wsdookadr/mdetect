package com.mdetect;

public class App {

	/*
	 * Overview:
	 * 
	 * Files with unknown checksums will be determined, and  
	 * a series of metrics will be computed on them. After that, a 
	 * set of rules would mark some of them as being suspicious.
	 * 
	 */

	 public static void main(String[] args) {
		 Detector d = new Detector();

	     //"/home/user/work/mdetect/samples/mod_system/adodb.class.php.txt");
	     //"/home/user/work/mdetect/samples/sample.php.txt");
		 //d.loadFile("/home/user/work/mdetect/samples/mod_system/pdo.inc.php.suspected");
		 //d.loadFile("/home/user/work/mdetect/data/wordpress/wp-includes/class-phpmailer.php");
		 d.loadFile("/home/user/work/mdetect/data/drupal/core/modules/datetime/src/Tests/DateTimeFieldTest.php");
		 d.runChecks();
	 }
	 
	 
}



package com.mdetect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.basex.query.QueryException;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMObjectModel;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;


public class App {
	/*
	 * Overview:
	 * 
	 * Files with unknown checksums will be determined, and  
	 * a series of metrics will be computed on them. After that, a 
	 * set of rules would mark some of them as being suspicious.
	 * 
	 * 
	 * Raising the stack limit is necessary because of serializing
	 * very nested structures ( -Xss3m ).
	 */

	 public static void main(String[] args) {
		Detector d = new Detector();
		XmlStore xstore = new XmlStore();
		/*
		 * small test .php files (between 20kb and 50kb)
		 * find data/ -name "*.php" -size +20000c -a -size -50000c
		 * 
		 */
		String smallerTestFiles[] = {
				"data/drupal/core/modules/system/src/Controller/DbUpdateController.php",
				"data/drupal/core/tests/Drupal/Tests/Core/Entity/Sql/SqlContentEntityStorageTest.php",
				"data/drupal/core/lib/Drupal/Core/Database/Driver/pgsql/Schema.php",
				"/home/user/work/mdetect/samples/mod_system/adodb.class.php.txt"
		};
		
		String largerTestFiles[] = {
				"/home/user/work/mdetect/samples/mod_system/adodb.class.php.txt",
				//"/home/user/work/mdetect/samples/sample.php.txt",
				"/home/user/work/mdetect/samples/mod_system/pdo.inc.php.suspected",
				//"/home/user/work/mdetect/data/wordpress/wp-includes/class-phpmailer.php",
				"/home/user/work/mdetect/data/drupal/core/modules/datetime/src/Tests/DateTimeFieldTest.php",
				//"/home/user/work/mdetect/data/wordpress/wp-includes/post.php",
				//"/home/user/work/mdetect/data/drupal/core/modules/migrate_drupal/tests/fixtures/drupal6.php"
		};
		/*
		for(String path: paths) {
			Utils.processAndStore(path, d, xstore);
		}
		try {
			String xqs = "db:open('xtrees')//functionCall//identifier//text()";
			String result = xstore.query(xqs);
			//System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		String testFiles[] = smallerTestFiles;
		AnalyzeTaskQueue tq = new AnalyzeTaskQueue(2,1000);
		for(int j=0;j<testFiles.length;j++) {
			System.out.println("producing task " + testFiles[j]);
			tq.produce(testFiles[j]);	
		}
		tq.shutdown();
		XmlStore.stopServer();
		
		System.exit(0);
	 }
	 
	 
}



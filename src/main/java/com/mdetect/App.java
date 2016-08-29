package com.mdetect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.basex.query.QueryException;
import org.eclipse.jgit.api.Git;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	 * Raising the stack limit is necessary because of serializing
	 * very nested structures ( -Xss3m ).
	 */
	
	 public static void acquireMetadata(Analyzer a, Detector d, XmlStore xstore,SqliteStore sq) {
			// retrieve checksums and metadata for a set of files and store them in the xml store
			List<String> gRepoPaths = a.findGitRepos("/home/user/work/mdetect/data");
			String testRepo = gRepoPaths.get(0);
			GitStore g = new GitStore(testRepo);
			List<GitTagDTO> gitTags = g.getAllTags();
			for(GitTagDTO tag: gitTags) {
				List<GitFileDTO> gitFiles = g.listHashes(tag.getTagCommit());
				for(GitFileDTO f: gitFiles) {
					//System.out.println(f.toString());
					xstore.addChecksum(f, tag.getTagName());
					sq.addChecksum(f, tag.getTagName());
				}
				sq.commit();
			}
			
	 }

	 public static void analyzeFileStructures(Analyzer a, Detector d, XmlStore xstore, SqliteStore sq) {
			/*
			 * to get files between 20kb and 50kb
			 * find data/ -name "*.php" -size +20000c -a -size -50000c
			 * 
			 */

			// parse and store parse trees in the xml store
			ArrayList<String> toAnalyze = (ArrayList<String>) a.findFilesToAnalyze("/home/user/work/mdetect/data");
			int analyzeQueueCapacity = 1000;
			int analyzeWorkers = 5;
			AnalyzeTaskQueue tq = new AnalyzeTaskQueue(analyzeWorkers,analyzeQueueCapacity,xstore);
			for(int j=0;j<toAnalyze.size();j++) {
				System.out.println("producing task " + toAnalyze.get(j));
				tq.produce(toAnalyze.get(j));
				tq.storePartialResultsInXMLStore();
			}
			tq.shutdown();
			tq.storePartialResultsInXMLStore();
	 }

	 public static void main(String[] args) {
		Analyzer a = new Analyzer();
		Detector d = new Detector();
		SqliteStore sq = new SqliteStore();
		XmlStore xstore = new XmlStore();
		sq.createSchema();
		
		acquireMetadata(a,d,xstore,sq);
		System.gc();
		//analyzeFileStructures(a,d,xstore,sq);
		

		XmlStore.stopServer();
		System.exit(0);
	 }
	 
	 
}



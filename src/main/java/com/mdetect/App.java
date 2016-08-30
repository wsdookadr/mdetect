package com.mdetect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.basex.query.QueryException;
import org.eclipse.jgit.api.Git;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

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
	 */
	 public static void acquireMetadata(String knownFilesPath, Analyzer a, Detector d, XmlStore xstore,SqliteStore sq) {
		/* 
		 * retrieve checksums and metadata for a set of files
		 * and store their checksums and metadata.
		 * 
		 * duplicates on (path,sha1) will be excluded.
		 */
		List<String> gRepoPaths = a.findGitRepos(knownFilesPath);
		for (String gRepo : gRepoPaths) {
			WriteQueue wq = new WriteQueue(xstore, sq);
			GitStore g = new GitStore(gRepo);
			List<GitTagDTO> gitTags = g.getAllTags();
			HashSet<String> dupeSet = new HashSet<String>();
			for (GitTagDTO tag : gitTags) {
				LinkedBlockingQueue<GitFileDTO> gitFiles = g.listHashes(tag.getTagCommit());
				for (GitFileDTO f : gitFiles) {
					String dupeKey = f.getPath() + f.getSha1();
					if(dupeSet.contains(dupeKey))
							continue;
					dupeSet.add(dupeKey);
					Pair<GitFileDTO, String> item = new ImmutablePair<GitFileDTO, String>(f, tag.getTagName());
					wq.produce(item);
				}
				System.out.println("tag="+tag.getTagName());
				System.gc();
			}
			wq.shutdown();
		}
	 }

	 public static void analyzeCodeStructure(String pathToAnalyze, Analyzer a, Detector d, XmlStore xstore, SqliteStore sq) {
		/*
		 * to get files between 20kb and 50kb find data/ -name "*.php" -size
		 * +20000c -a -size -50000c
		 * 
		 */
		/* parse and store parse trees in the xml store */
		ArrayList<String> toAnalyze = (ArrayList<String>) a.findFilesToAnalyze(pathToAnalyze);
		int analyzeQueueCapacity = 1000;
		int analyzeWorkers = 5;
		AnalyzeTaskQueue tq = new AnalyzeTaskQueue(analyzeWorkers, analyzeQueueCapacity, xstore, "/unknown/");
		for (int j = 0; j < toAnalyze.size(); j++) {
			System.out.println("producing task " + toAnalyze.get(j));
			tq.produce(toAnalyze.get(j));
			tq.storePartialResultsInXMLStore();
		}
		tq.shutdown();
		/*
		 * store the resutls that were computed after all the work units were
		 * sent out, and the executor was shut down (those were not drained in
		 * the loop above because they were still processing after that loop
		 * finished, so we collect the remaining ones below)
		 */
		tq.storePartialResultsInXMLStore();
	 }

	 public static void main(String[] args) {
		Analyzer a = new Analyzer();
		Detector d = new Detector();
		SqliteStore sq = new SqliteStore();
		XmlStore xstore = new XmlStore();
		sq.createSchema();
		
		//acquireMetadata("/home/user/work/mdetect/data", a,d,xstore,sq);
		System.gc();
		analyzeCodeStructure("/home/user/work/mdetect/samples",a,d,xstore,sq);

		XmlStore.stopServer();
		System.exit(0);
	 }
	 
	 
}



package com.mdetect;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


public class App {
	/*
	 * Overview:
	 * 
	 * Files with unknown checksums will be determined, and  
	 * a series of metrics will be computed on them. After that, a 
	 * set of rules would mark some of them as being suspicious.
	 * 
	 */
	
	@SuppressWarnings("deprecation")
	private static Map<String, String> parseCmdLineParams(String[] args) {
		HashMap<String, String> cmdLineParams = new HashMap<String, String>();
		CommandLineParser parser = new PosixParser();
		Options options = new Options();
		options.addOption(
				OptionBuilder
				.withLongOpt("checksum")
				.hasArg(true)
				.withDescription("path to PHP source code to be checksumed and whitelisted")
				.create('c')
				);
		options.addOption(
				OptionBuilder
				.withLongOpt("detect")
				.hasArg(true)
				.withDescription("path to PHP code to be analyzed")
				.create('d')
				);
		HelpFormatter formatter = new HelpFormatter();
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		String checkPath = new String();
		String detectPath = new String();
		
		if(line.hasOption("checksum") && line.hasOption("detect")) {
			System.err.println("[ERROR] Both checksum and detect options were provided. Only one of them should be passed.");
			formatter.printHelp("libreurl", options);
			System.exit(2);
		} else if(line.hasOption("checksum")) {
			cmdLineParams.put("checkPath", line.getOptionValue("checksum"));
		} else if(line.hasOption("detect")) {
			cmdLineParams.put("detectPath", line.getOptionValue("detect"));
		} else {
			System.err.println("[ERROR] No parameters were provided");
			formatter.printHelp("libreurl", options);
		}
		return cmdLineParams;
	}
	
	 public static void acquireMetadata(
			 String knownFilesPath,
			 Analyzer a,
			 Detector d,
			 XmlStore xstore,
			 SqliteStore sq) {
		/* 
		 * retrieve checksums and metadata for a set of files
		 * and store their checksums and metadata in the sqlite
		 * data store.
		 * 
		 * duplicates on (path,sha1) will be excluded.
		 */
		List<String> gRepoPaths = a.findGitRepos(knownFilesPath);
		for (String gRepo : gRepoPaths) {
			WriteQueue wq = new WriteQueue(xstore, sq);
			GitStore g = new GitStore(gRepo);
			List<GitTagDTO> gitTags = g.getAllTags();
			HashSet<String> dupeSet = new HashSet<String>();
			g.reset();
			for(GitTagDTO tag : gitTags) {
				LinkedBlockingQueue<GitFileDTO> gitFiles = g.listHashes(tag.getTagCommit());
				for(GitFileDTO f : gitFiles) {
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

	public static void analyzeCodeStructure(
			String pathToAnalyze,
			Analyzer a,
			Detector d,
			XmlStore xstore,
			SqliteStore sq) {
		/* parse and store parse trees in the xml store */
		ArrayList<String> toAnalyze = (ArrayList<String>) a.findFilesToAnalyze(pathToAnalyze);
		int analyzeQueueCapacity = 1000;
		int analyzeWorkers = 3;
		AnalyzeTaskQueue tq = new AnalyzeTaskQueue(analyzeWorkers, analyzeQueueCapacity, xstore, "/unknown/");
		for (int j = 0; j < toAnalyze.size(); j++) {
			System.out.println("producing task " + toAnalyze.get(j));
			tq.produce(toAnalyze.get(j));
			tq.storePartialResultsInXMLStore();
		}
		tq.shutdown();
	}

	public static void main(String[] args) throws Exception {
		Map<String, String> cmdLineParams = parseCmdLineParams(args);
		Analyzer a = new Analyzer();
		Detector d = new Detector();
		SqliteStore sq = new SqliteStore();
		XmlStore xstore = new XmlStore();
		ASTCheckRunner cr = new ASTCheckRunner(xstore);
		sq.createSchema();

		if(cmdLineParams.containsKey("detectPath")) {
			String path = cmdLineParams.get("detectPath");
			analyzeCodeStructure(path,a,d,xstore,sq);	
		}else if(cmdLineParams.containsKey("checkPath")) {
			String path = cmdLineParams.get("checkPath");
			acquireMetadata(path,a,d,xstore,sq);
		}
		
		xstore.stopServer();
		//cr.check1();
		System.exit(0);
	 }
	 
}



package com.mdetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.BasicConfigurator;
import org.basex.query.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {
	/*
	 * Overview:
	 * 
	 * Files with unknown checksums will be determined, and  
	 * a series of checks will be run on them. After that, a 
	 * set of rules would mark some of them as being suspicious.
	 * 
	 */
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	
	@SuppressWarnings("deprecation")
	private static Map<String, String> parseCmdLineParams(String[] args) {
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
		options.addOption(
				OptionBuilder
				.withLongOpt("report")
				.hasArg(false)
				.withDescription("report potentially malicious files")
				.create('r')
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
		HashMap<String, String> cmdLineParams = new HashMap<String, String>();
		if(line.hasOption("checksum") && line.hasOption("detect")) {
			logger.error("[ERROR] Both checksum and detect options were provided. Only one of them should be passed.");
			formatter.printHelp("mdetect", options);
			System.exit(2);
		} else if(line.hasOption("checksum")) {
			cmdLineParams.put("checkPath", line.getOptionValue("checksum"));
		} else if(line.hasOption("detect")) {
			cmdLineParams.put("detectPath", line.getOptionValue("detect"));
		} else if(line.hasOption("report")) {
			cmdLineParams.put("report", "1");
		} else {
			logger.error("[ERROR] No parameters were provided");
			formatter.printHelp("libreurl", options);
		}
		return cmdLineParams;
	}
	
	 public static void acquireChecksums(
			 String knownFilesPath,
			 XmlStore xstore,
			 SqliteStore sq) {
		/* 
		 * retrieve checksums and metadata for a set of files
		 * and store their checksums and metadata in the sqlite
		 * data store.
		 * duplicates on (path,sha1) will be excluded.
		 * 
		 * writing to the database will be made in chunks.
		 */
		GitFileIterator gi = new GitFileIterator(knownFilesPath);
		int added = 0;
		while(gi.hasNext()) {
			Pair<GitFileDTO, String> p = gi.next();
			if(!p.getLeft().getPath().endsWith(".php"))
				continue;
			logger.info(p.getLeft().getPath());
			sq.addChecksum(p.getLeft(), p.getRight());
			added += 1;
			if (added == 500) {
				sq.commit();
				added = 0;
			}
		}
		if (added > 0) {
			sq.commit();
		}
	}

	public static void analyzeCodeStructure(
			String pathToAnalyze,
			XmlStore xstore,
			SqliteStore sq) {
		/* parse and store parse trees in the xml store */
		ArrayList<String> toAnalyze = (ArrayList<String>) FileScanUtils.findFilesToAnalyze(pathToAnalyze);
		AnalyzeTimedTaskQueue tq = new AnalyzeTimedTaskQueue(3, xstore, "/unknown/");
		tq.process();
		tq.gatherAndClean();
		for (int j = 0; j < toAnalyze.size(); j++) {
			tq.produce(toAnalyze.get(j));
		}
		tq.close();
		tq.printPerfReport();
	}
	
	public static void report(XmlStore xstore) {
		try {
			xstore.stopServer();
			System.out.println(xstore.eval(Utils.getResource("/report.xql")));
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		Map<String, String> cmdLineParams = parseCmdLineParams(args);
		SqliteStore sq = new SqliteStore();
		XmlStore xstore = new XmlStore();
		sq.createSchema();

		if(cmdLineParams.containsKey("detectPath")) {
			String path = cmdLineParams.get("detectPath");
			analyzeCodeStructure(path,xstore,sq);
			xstore.stopServer();
		} else if(cmdLineParams.containsKey("checkPath")) {
			String path = cmdLineParams.get("checkPath");
			acquireChecksums(path,xstore,sq);
			xstore.stopServer();
		} else if(cmdLineParams.containsKey("report")) {
			report(xstore);
		}
		
		System.exit(0);
	 }
}



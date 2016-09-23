package com.mdetect;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/*
 * TaskWorker derived class that takes care of
 * processing files: parsing the file.
 * 
 */
public class AnalyzeTimedTaskWorker extends AbstractTimedTaskWorker<String> {
	public Detector d = null;
    private XmlStore xstore = null;
	private static final Logger logger = LoggerFactory.getLogger(AnalyzeTimedTaskWorker.class);
	
	/*
	 * since this worker is run in its separate thread, there will be
	 * one sqlite connection per thread.
	 * https://www.sqlite.org/threadsafe.html
	 */
	private SqliteStore sq = null;
	private String storePrefix;
	/*
	 * The constructor receives the work unit which is a file path,
	 * and the xml store.
	 */
	public AnalyzeTimedTaskWorker(String workUnit, XmlStore xstore) {
		super(workUnit);
		this.sq = new SqliteStore();
		this.xstore = xstore;
		this.d = new Detector();
	}

	/*
	 * The file's checksum is looked up in the db.
	 * If it's not found it's a valid work unit, and we analyze it
	 * and return back the result, otherwise we return null since it's
	 * a known file (if it's stored in sqlite).
	 * 
	 */
	@Override
	public Integer call() {
		logger.info("[DBG]  started TaskWorker, workUnit=" + workUnit);
		try {
			String sha1 = Utils.gitHash(workUnit);
			ParseTreeDTO result = null;
			logger.info("[DBG] sha1=" + sha1 + " file="+workUnit);
			if(!sq.hasChecksum(sha1)) {
				Document processedDoc = d.processFile(workUnit);
				result = new ParseTreeDTO(processedDoc, workUnit, "");
				String contentsToInsert = "";
				try {
					contentsToInsert = Utils.serializeDOMDocument(result.getD());
					xstore.add(storePrefix + result.getFilePath(), contentsToInsert, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				logger.info("[DBG] known file "+workUnit);
			}
		} catch (CancellationException e) {
			logger.info("[DBG] received cancellation exception");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("[DBG] finished TaskWorker, workUnit=" + workUnit);
		return 0;
	}

}

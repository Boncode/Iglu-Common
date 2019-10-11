/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.logging.module;

import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.time.SafeDateFormat;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 */
public class RotatingFileLogger extends SimpleFileLogger implements Pageable {


	private int nrofLogFilesToKeep = 7;
	private int logRotateIntervalInHours = 24;
	public static SafeDateFormat TIMESTAMP_LOGFILE_FORMAT = new SafeDateFormat("yyyy_MM_dd_HH_mm");


	public RotatingFileLogger(String fileName) {
		super(fileName);
	}



	/**
	 * @param date
	 * @return
	 */
	private String getFileName(Date date) {
		String dateStr = null;
		if (date != null) {
			dateStr = TIMESTAMP_LOGFILE_FORMAT.format(date);
		}
		return fileName + (dateStr != null ? '.' + dateStr : "") + ".log";
	}

	@Override
	public int getPageIntervalInMinutes() {
		return logRotateIntervalInHours * 60;
	}

	@Override
	public int getPageOffsetInMinutes() {
		return 0;
	}

	public void setProperties(Properties properties) {
		super.setProperties(properties);
		nrofLogFilesToKeep = Integer.parseInt(properties.getProperty("nr_log_files_to_keep", "" + nrofLogFilesToKeep));
		logRotateIntervalInHours = Integer.parseInt(properties.getProperty("rotate_interval_hours", "" + logRotateIntervalInHours));
		rotateIfFileTooOldAtStartup();
	}


	@Override
	public void onPageEvent(long officialTime) {
		synchronized (lock) {
			stop();

			Date rotateDate = new Date(officialTime);
			//save current log with date
			LogEntry errorLogEntry = rotate(rotateDate);

			//start with new log file
			start();
			clearOutdatedFiles();
			if(errorLogEntry != null) {
				log(errorLogEntry);
			}
		}

		Date officialDate = new Date(officialTime - (nrofLogFilesToKeep * logRotateIntervalInHours * 60 * 60 * 1000));
		String destLogFileName = getFileName(officialDate);
		File obsoleteFile = new File(destLogFileName);
		if (obsoleteFile.exists()) {
			obsoleteFile.delete();
		}

	}

	private void clearOutdatedFiles() {
		long now = System.currentTimeMillis();
		long clearingDate = now - (nrofLogFilesToKeep * logRotateIntervalInHours * 60 * 60 * 1000);
		File file = new File(fileName);
		String dirName = file.getParent();
		File dir = new File(dirName);
		File[] files = dir.listFiles();
		for(File fileInDir : files) {
			if (fileInDir.getName().endsWith(".zip")) {
				if(fileInDir.lastModified() < clearingDate) {
					System.out.println(new LogEntry(Level.VERBOSE, "clearing outdated log file " + fileInDir));
					fileInDir.delete();
				}
			}
		}
	}


	public LogEntry rotate(Date rotateDate) {
		LogEntry errorLogEntry = null;
		File file = new File(fileName + ".log");
		file.renameTo(new File(getFileName(rotateDate)));
		file = new File(getFileName(rotateDate));
		try {
			FileSupport.zip(file);
			file.delete();
		} catch (IOException e) {
			errorLogEntry = new LogEntry(Level.CRITICAL,"cannot zip file " + file, e);
			e.printStackTrace();
		}
		return errorLogEntry;
	}

	private void rotateIfFileTooOldAtStartup() {

		File file = new File(fileName + ".log");
		if(file.exists()) {
			try {
				//VBS 20191003 06:34:11.227
				String firstLine = FileSupport.getFirstLineInText(file);
				Date prevDate = getDateFromLogLine(firstLine);
				long now = System.currentTimeMillis();
				if (prevDate.getTime() + logRotateIntervalInHours * 60 * 60 * 1000 < now) {
					rotate(prevDate);
				}
				clearOutdatedFiles();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException pe) {
				pe.printStackTrace();
			}
		}
	}

	public static Date getDateFromLogLine(String logLine) throws ParseException {
		String dateString = logLine.substring(4, 18);
		return new SimpleDateFormat(LogEntry.DEFAULT_DATE_FORMAT).parse(dateString);
	}


}

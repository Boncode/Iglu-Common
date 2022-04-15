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

import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.logging.LogPrintStream;
import org.ijsberg.iglu.logging.Logger;
import org.ijsberg.iglu.util.ResourceException;
import org.ijsberg.iglu.util.io.FileSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class SimpleFileLogger implements Logger, Startable {

	private boolean isStarted = false;
	private int logLevelOrdinal = Level.DEBUG.ordinal();
	private String dateFormat = LogEntry.DEFAULT_TIMESTAMP_FORMAT;
	private int entryOriginStackTraceDepth = 0;

	private boolean printOrigin = false;

	private PrintStream originalSystemOut;
	private PrintStream filteredSystemOut;
	protected PrintStream logFilePrintStream;

	protected String fileName;
	protected Object lock = new Object();

	protected List<Logger> appenders;

	public void setPrintOrigin(boolean printOrigin) {
		this.printOrigin = printOrigin;
	}

	public SimpleFileLogger(String fileName) {
		this.fileName = fileName;
		originalSystemOut = System.out;
		filteredSystemOut = new LogPrintStream(originalSystemOut, this);
	}

	protected void openLogStream() {
		try {
			File file = new File(fileName + ".log");
			if(file.exists() && file.canWrite()) {
				logFilePrintStream = new PrintStream(new FileOutputStream(file, true));
			} else {
				logFilePrintStream = new PrintStream(new FileOutputStream(FileSupport.createFile(fileName + ".log")));
			}
		}
		catch (IOException e) {
			throw new ResourceException("unable to open new logfile '" + fileName + ".log'", e);
		}
	}

	public SimpleFileLogger(PrintStream logFilePrintStream) {
		originalSystemOut = System.out;
		filteredSystemOut = new LogPrintStream(originalSystemOut, this);
		this.logFilePrintStream = logFilePrintStream;
	}


	public void log(LogEntry entry) {
		if (entry.getLevel().ordinal() >= logLevelOrdinal) {
			synchronized (lock) {
				writeEntry(entry);
			}
		}
		if(appenders != null) {
			for(Logger appender : appenders) {
				appender.log(entry);
			}
		}
	}

	public void writeEntry(LogEntry entry) {
		logFilePrintStream.println(getPrefix(entry) +
				(entry.getMessage() != null ? " " + entry.getMessage() : ""));

		if (entry.getData() != null) {
			logFilePrintStream.println(entry.getData());
			if (entry.getData() instanceof Throwable) {
				printThrowable((Throwable) entry.getData());
				Throwable cause = ((Throwable) entry.getData()).getCause();
				while (cause != null) {
					logFilePrintStream.println();
					logFilePrintStream.println("caused by:");
					printThrowable(cause);
					cause = cause.getCause();
				}
			}
		}

		if (entryOriginStackTraceDepth > 0) {
			printStackTrace(getStackTracePart(4, entryOriginStackTraceDepth));
		}
	}

	private String getPrefix(LogEntry entry) {
		String originToPrint = "";
		if(printOrigin) {
			StackTraceElement element = getStackTracePart(7, 1)[0];
			originToPrint = "[" + element.getClassName() + "] ";
		}

		return originToPrint + entry.getLevel().getShortDescription() + " " +
				new SimpleDateFormat(dateFormat).format(new Date(entry.getTimeInMillis()));
	}

	private void printThrowable(Throwable t) {
		logFilePrintStream.println(t.getClass().getSimpleName() + " : " + t.getMessage());
		printStackTrace(t.getStackTrace());
	}

	private void printStackTrace(StackTraceElement[] stackTrace) {
		for (int i = 0; i < stackTrace.length; i++) {
			logFilePrintStream.println("at " + stackTrace[i]);
		}
	}

	public static StackTraceElement[] getStackTracePart(int start, int desiredLength) {
		StackTraceElement[] fullStackTrace = Thread.currentThread().getStackTrace();
		for (int nrofGetSTCalls = 0; nrofGetSTCalls <= fullStackTrace.length; nrofGetSTCalls++) {
			if (!"java.lang.Thread".equals(fullStackTrace[nrofGetSTCalls].getClassName())) {
				return getStackTracePart(fullStackTrace, start + nrofGetSTCalls, desiredLength);
			}
		}
		throw new RuntimeException("stack trace contains only java.lang.Thread.getStackTrace calls");
	}

	public static StackTraceElement[] getStackTracePart(StackTraceElement[] fullStackTrace, int start, int desiredLength) {
		int actualLength = desiredLength;
		if (fullStackTrace.length - start < desiredLength) {
			actualLength = fullStackTrace.length - start;
		}
		if (actualLength < 0) {
			actualLength = 0;
		}
		StackTraceElement[] desiredStackTrace = new StackTraceElement[actualLength];
		if (actualLength == 0) {
			return desiredStackTrace;
		}
		System.arraycopy(fullStackTrace, start, desiredStackTrace, 0, actualLength);
		return desiredStackTrace;
	}

	public static String formatArray(Object[] array) {
		StringBuffer retval = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			retval.append(array[i].toString()).append('\n');
		}
		return retval.toString();
	}

	public void setLogLevel(String name) {
		logLevelOrdinal = Level.valueOf(name).ordinal();
	}

	public int getLogLevelOrdinal() {
		return logLevelOrdinal;
	}


	public void start() {
		openLogStream();
		System.out.println(new LogEntry("starting file logging to " + this.fileName));
		synchronized (lock) {
			System.setOut(filteredSystemOut);
			isStarted = true;
		}
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setEntryOriginStackTraceDepth(int entryOriginStackTraceDepth) {
		this.entryOriginStackTraceDepth = entryOriginStackTraceDepth;
	}

	public void stop() {
		synchronized (lock) {
			System.out.println(new LogEntry("stopping file logging to " + this.fileName));
			isStarted = false;
			System.setOut(originalSystemOut);
			logFilePrintStream.close();
		}
	}

	public synchronized boolean isStarted() {
		return isStarted;
	}


	public void setProperties(Properties properties) {

		logLevelOrdinal = Arrays.asList(Level.LEVEL_CONFIG_TERM).indexOf(properties.getProperty("log_level", Level.LEVEL_CONFIG_TERM[logLevelOrdinal]));
		entryOriginStackTraceDepth = Integer.parseInt(properties.getProperty("entry_stack_trace_depth", "" + entryOriginStackTraceDepth));
		System.out.println(new LogEntry("log level set to " + Level.LEVEL_CONFIG_TERM[logLevelOrdinal]));
	}


	@Override
	public void  addAppender(Logger appender) {
		if(appenders == null) {
			appenders = new ArrayList<>();
		}
		appenders.add(appender);
	}

	@Override
	public void removeAppender(Logger appender) {
		if(appenders != null) {
			appenders.remove(appender);
		}
	}

}

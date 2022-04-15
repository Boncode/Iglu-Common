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

package org.ijsberg.iglu.invocation;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.ResourceException;
import org.ijsberg.iglu.util.io.FileSupport;

import java.io.*;
import java.util.Properties;

/**
 * Processes command-line instructions that are read from an input file.
 * Writes results to an output file.
 * <p/>
 * Configuration may provide:
 * <ul>
 * <li>input_file_location (default: console/input.txt)</li>
 * <li>output_file_location (default: console/output.txt)</li>
 * <li>temp_output_file_location (default: console/tempoutput.txt)</li>
 * </ul>
 * <p/>
 * The output file is temporary during the processing phase. It gets renamed to its
 * final name once processing is done. The appearance of the (final) output file may act as
 * a trigger for scripts to display or evaluate the result.
 * The instructions passed are considered to be internal requests.
 * Access to the input file should therefore be restricted to the OS-user running the application.
 */
public class RootConsole extends CommandLineProcessor implements Startable, Runnable {
	private Thread thread;
	private boolean isRunning = false;
	private BufferedReader commandLineInput;
	private String baseDir = ".";
	private String inputFileLocation = "console/input.txt";
	private String outputFileLocation = "console/output.txt";
	private String tempOutputFileLocation = "console/temp_output.txt";

	public RootConsole(Assembly assembly) {
		super(assembly);
	}


	/**
	 * Determines file locations.
	 */
	public void setProperties(Properties properties) {
		baseDir = properties.getProperty("base_dir", baseDir);
		inputFileLocation = baseDir + '/' + properties.getProperty("input_file_location", inputFileLocation);
		outputFileLocation = baseDir + '/' + properties.getProperty("output_file_location", outputFileLocation);
		tempOutputFileLocation = baseDir + '/' + properties.getProperty("temp_output_file_location", tempOutputFileLocation);
	}

	/**
	 * Starts monitoring the input file.
	 */
	public void start() {
		System.out.println(new LogEntry("starting root console"));
		try {
			openFileReader();
		} catch (IOException ioe) {
			throw new ResourceException("can not open file reader", ioe);
		}
		thread = new Thread(this);
		isRunning = true;
		thread.start();
	}

	public boolean isStarted() {
		return isRunning;
	}

	/**
	 * Stops monitoring the input file.
	 */
	public void stop() {
		isRunning = false;
		thread.interrupt();
		try {
			closeFileReader();
		} catch (IOException ioe) {
			System.out.println(new LogEntry(Level.CRITICAL, "I/O exception while closing file reader: " + ioe.getMessage(), ioe));
		}
	}

	/**
	 *
	 */
	private void openFileReader() throws IOException {
		File inputFile = FileSupport.createFile(inputFileLocation);
		inputFile.delete();
		inputFile.createNewFile();
		commandLineInput = new BufferedReader(new FileReader(inputFile));
	}

	/**
	 *
	 */
	private void resetFileReader() throws IOException {
		closeFileReader();
		openFileReader();
	}

	/**
	 *
	 */
	private void closeFileReader() throws IOException {
		if (commandLineInput != null) {
			commandLineInput.close();
		}
	}

	/**
	 *
	 */
	public void run() {
		try {
			while (isRunning) {
				String commandLine = commandLineInput.readLine();
				if (commandLine != null) {
					processCommandLineSafely(commandLine);
				}
				try {
					Thread.sleep(25);
				} catch (InterruptedException ie) {
					System.out.println(new LogEntry("console interrupted"));
					isRunning = false;
				}
			}
		} catch (IOException ioe) {
			System.out.println(new LogEntry(Level.CRITICAL, "console interrupted by I/O exception: " + ioe.getMessage(), ioe));
			stop();
		}
	}

	private void processCommandLineSafely(String commandLine) throws IOException {
		Object result;
		try {
			result = processCommandLine(commandLine);
			System.out.println(new LogEntry("command line '" + commandLine + "' successfully processed"));
		} catch (Throwable t) {
			result = t;
			System.out.println(new LogEntry(Level.CRITICAL, "console can not process command line '" + commandLine + "'", t));
		}
		writeResult(result);
	}

	/**
	 * Writes result to the output file.
	 *
	 * @param result
	 */
	private void writeResult(Object result) throws IOException {
		File outputFile = new File(outputFileLocation);
		File tempfile = FileSupport.createFile(tempOutputFileLocation);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(tempfile);
			PrintStream ps = new PrintStream(new FileOutputStream(tempfile));
			if (result != null) {
				if (result instanceof Throwable) {
					((Throwable) result).printStackTrace(ps);
				}
				ps.println(result.toString());
			}
			ps.close();
		} finally {
			fos.close();
		}

		if (outputFile.exists()) {
			boolean success = outputFile.delete();
		}

		boolean success = tempfile.renameTo(new File(outputFileLocation));
	}
}

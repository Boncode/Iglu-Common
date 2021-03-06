/*
 * Copyright 2011-2013 Jeroen Meetsma - IJsberg
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

import org.ijsberg.iglu.util.io.FileSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

/**
 */
public class RootConsoleTest extends CommandLineProcessorTest {

	private File tmpDir;
	private RootConsole console;


	@Before
	public void setUp() throws Exception {
		setUpAssembly();
		tmpDir = FileSupport.createTmpDir("RootConsoleTest");
		console = new RootConsole(assembly);
		Properties consoleProperties = new Properties();
		consoleProperties.setProperty("base_dir", tmpDir.getPath());
		console.setProperties(consoleProperties);
		console.start();
		//wait for async process to start (FIXME this has caused an error once)
		Thread.sleep(100);
	}

	@After
	public void tearDown() throws Exception {
		console.stop();
		tmpDir.delete();
	}

	@Test
	public void testIsStarted() throws Exception {
		System.out.println(tmpDir.getAbsolutePath());
		assertEquals(1, tmpDir.list().length);
		File consoleDir = tmpDir.listFiles()[0];
		assertEquals(1, consoleDir.list().length);

		File inputFile = consoleDir.listFiles()[0];
		PrintStream ps = getPrintStream(inputFile);

		ps.println("cluster.object.getMessage(\"World!\")");
		//wait for async process to finish
		Thread.sleep(150);

		assertEquals(2, consoleDir.list().length);
		String output = new String(FileSupport.getBinaryFromFS(consoleDir.getAbsolutePath() + "/output.txt")).trim();
		assertEquals("Hello \"World!\"", output.trim());

		//reset file
//		closeOutputStream();
//		ps = getPrintStream(inputFile);

		ps.println("cluster.object.bogus(\"World!\")");
		//wait for async process to finish
		Thread.sleep(150);
		output = new String(FileSupport.getBinaryFromFS(consoleDir.getAbsolutePath() + "/output.txt")).trim();

//		System.out.println(output);
		assertEquals("java.lang.NoSuchMethodException:", output.substring(0, 32));

		fos.close();


	}

	private FileOutputStream fos;

	public PrintStream getPrintStream(File file) throws FileNotFoundException {
		fos = new FileOutputStream(file);
		PrintStream ps = new PrintStream(fos);
		return ps;
	}

	public void closeOutputStream() throws IOException {
		fos.close();
	}

}

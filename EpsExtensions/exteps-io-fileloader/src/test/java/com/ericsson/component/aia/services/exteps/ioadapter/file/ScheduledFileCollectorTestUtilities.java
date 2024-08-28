/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.ioadapter.file;

import java.io.File;
import java.io.IOException;

import org.junit.rules.TemporaryFolder;

public class ScheduledFileCollectorTestUtilities {
	
	public static void createhiddenFile(final TemporaryFolder folder, final String filename)
			throws IOException, InterruptedException {
		if (isLinux()) {
			createHiddenFileLinux(filename, folder);
		} else {
			createHiddenFileWindows(filename, folder);
		}
	}
	
	static void createHiddenFileLinux(final String filename, final TemporaryFolder folder) throws IOException {
		folder.newFile("." + filename);
	}
	
	/**
	 * Because Windows uses attributes to create a hidden file, it isn't as simple as creating a file with dot. This
	 * method will create a hidden file, and wait for it to be created (there is no guarentee that
	 * Runtime.getRuntime().exec will have finished executing before this method returns, so we wait (up to 5 seconds)
	 * to make sure it is created and hidden.
	 * 
	 * @param filename
	 * @param folder
	 *            TODO
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	static void createHiddenFileWindows(final String filename, final TemporaryFolder folder)
			throws InterruptedException, IOException {
		folder.newFile(filename);
		Runtime.getRuntime().exec("attrib +h " + folder.getRoot() + File.separator + filename);
		final File hiddenFile = new File(folder.getRoot() + File.separator + filename);
		int count = 0;
		while (!hiddenFile.isHidden() && count++ < 5) {
			Thread.sleep(1000);
		}
	}
	
	static boolean isLinux() {
		final String operativeSystem = System.getProperty("os.name").toLowerCase();
		return operativeSystem.indexOf("nix") >= 0 || operativeSystem.indexOf("nux") >= 0
				|| operativeSystem.indexOf("aix") >= 0;
	}
}

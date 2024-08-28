package com.ericsson.component.aia.services.exteps.util;

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.rules.TemporaryFolder;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;

public class EpsTestUtil {

    private final EpsProvider provider = EpsProvider.getInstance();

    private ExecutorService execService = Executors.newSingleThreadExecutor();

    private EpsInstanceManager epsInstanceManager;

    public EpsInstanceManager createEpsInstanceInNewThread() throws InterruptedException, ExecutionException {

        // Added for tests that share EpsTestUtil instances through inheritance,
        // like S1 and X2 correlations.
        if (execService.isShutdown()) {
            execService = Executors.newSingleThreadExecutor();
        }

        final Future<EpsInstanceManager> epsFuture = execService.submit(new Callable<EpsInstanceManager>() {

            @Override
            public EpsInstanceManager call() {
                epsInstanceManager = EpsInstanceManager.getInstance();
                epsInstanceManager.start();
                return epsInstanceManager;
            }

        });

        return epsFuture.get();
    }

    public EpsInstanceManager getEpsInstanceManager() {
        return epsInstanceManager;
    }

    public void shutdownEpsInstance() {
        EpsInstanceManager.getInstance().stop();
        execService.shutdownNow();
        provider.clean();
    }

    public String deployModule(final InputStream resource) {
        if (epsInstanceManager == null) {
            throw new IllegalStateException("EPS not started!");
        }
        final ModuleManager manager = epsInstanceManager.getModuleManager();
        return manager.deployModuleFromFile(resource);
    }

    public static void createhiddenFile(final TemporaryFolder folder, final String subDir, final String filename) throws IOException,
            InterruptedException {
        if (isLinux()) {
            createHiddenFileLinux(folder, subDir, filename);
        } else {
            createHiddenFileWindows(folder, subDir, filename);
        }
    }

    static void createHiddenFileLinux(final TemporaryFolder folder, final String subDir, final String filename) throws IOException {
        folder.newFile(subDir + File.separator + "." + filename);
    }

    /**
     * Because Windows uses attributes to create a hidden file, it isn't as simple as creating a file with dot. This method will create a hidden file,
     * and wait for it to be created (there is no guarentee that Runtime.getRuntime().exec will have finished executing before this method returns, so
     * we wait (up to 5 seconds) to make sure it is created and hidden.
     * 
     * @param folder
     *            TODO
     * @param subDir
     *            TODO
     * @param filename
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    static void createHiddenFileWindows(final TemporaryFolder folder, final String subDir, final String filename) throws IOException,
            InterruptedException {
        folder.newFile(subDir + File.separator + filename);
        final String filePath = folder.getRoot() + File.separator + subDir + File.separator + filename;
        Runtime.getRuntime().exec("attrib +h " + filePath);
        final File hiddenFile = new File(filePath);
        int count = 0;
        while (!hiddenFile.isHidden() && count++ < 5) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                throw e;
            }
        }
    }

    static boolean isLinux() {
        final String operativeSystemName = System.getProperty("os.name").toLowerCase();
        return operativeSystemName.indexOf("nix") >= 0 || operativeSystemName.indexOf("nux") >= 0 || operativeSystemName.indexOf("aix") >= 0;
    }

}

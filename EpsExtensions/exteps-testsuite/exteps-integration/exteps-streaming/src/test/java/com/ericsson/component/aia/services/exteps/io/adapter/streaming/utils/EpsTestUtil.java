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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils;

import java.io.InputStream;
import java.util.concurrent.*;

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
        return manager.deployModule(resource);
    }

}

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
import java.util.List;

public class ScheduledFileCollectorNoDotLast extends ScheduledFileCollector {

    /**
     * @param directoryArray
     * @param fileRegex
     * @param fileCollectionInputAdapter
     */
    public ScheduledFileCollectorNoDotLast(final String fileRegex, final FileCollectionInputAdapter fileCollectionInputAdapter) {
        super(fileRegex, fileCollectionInputAdapter);
    }

    @Override
    protected List<File> processFilesInDirectory(final File directory) {
        return listFilesInDirectory(directory);
    }

    @Override
    protected boolean fileShouldBeCollected(final File file) {
        return !file.isHidden();
    }

}

package com.ericsson.component.aia.services.exteps.file;

import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

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

public class FileIoAdapterReceiver implements OutputAdapter {

    @Override
    public boolean understandsURI(final String uri) {
        return "test_file_io_output:/".equals(uri);
    }

    @Override
    public void onEvent(final Object inputEvent) {
    }

    @Override
    public void init(final EventHandlerContext ctx) {
    }

    @Override
    public void destroy() {
    }
}
